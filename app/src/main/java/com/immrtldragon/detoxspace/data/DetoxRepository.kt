package com.immrtldragon.detoxspace.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.immrtldragon.detoxspace.data.local.InvitationDao
import com.immrtldragon.detoxspace.data.local.InvitationEntity
import com.immrtldragon.detoxspace.data.remote.CreateInvitationRequest
import com.immrtldragon.detoxspace.data.remote.DetoxApi
import com.immrtldragon.detoxspace.data.remote.RespondInvitationRequest
import com.immrtldragon.detoxspace.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface DetoxRepository {
    val connections: Flow<List<Connection>>
    val invitations: Flow<List<SentSignal>>
    val presence: Flow<Presence>
    fun setPresence(value: Presence)
    suspend fun syncConnections()
    suspend fun syncAll()
    suspend fun retryPending()
    suspend fun registerPushToken(token: String)
    suspend fun createConnectionCode(): String
    suspend fun acceptConnectionCode(code: String)
    suspend fun devices(): List<DeviceSession>
    suspend fun revokeDevice(id: String): Boolean
    suspend fun removeConnection(userId: String)
    suspend fun blockConnection(userId: String)
    suspend fun sendInvitation(connection: Connection, signal: SignalType, window: TimeWindow, note: String?)
    suspend fun updateInvitation(id: String, state: InvitationState)
}

@Singleton
class OfflineFirstDetoxRepository @Inject constructor(
    private val invitationDao: InvitationDao,
    private val api: DetoxApi,
    private val sessionStore: SessionStore,
    @ApplicationContext private val context: Context,
) : DetoxRepository {
    private val localPresence = MutableStateFlow(Presence.AVAILABLE)
    override val presence: Flow<Presence> = localPresence
    private val remoteConnections = MutableStateFlow<List<Connection>>(emptyList())
    override val connections: Flow<List<Connection>> = remoteConnections
    override val invitations: Flow<List<SentSignal>> = invitationDao.observeAll().map { rows ->
        rows.map { row ->
            SentSignal(
                id = row.id,
                connectionName = row.connectionName,
                signalTitle = row.signalTitle,
                sentAt = Instant.ofEpochMilli(row.createdAtEpochMillis).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("h:mm a")),
                state = InvitationState.valueOf(row.state),
                timeWindow = row.timeWindow,
                isIncoming = row.isIncoming,
                note = row.note,
            )
        }
    }

    override fun setPresence(value: Presence) { localPresence.value = value }

    override suspend fun syncConnections() {
        remoteConnections.value = api.connections().connections.map { item ->
            Connection(
                id = item.user_id,
                name = item.username,
                initials = item.username.take(2).uppercase(),
                presence = Presence.AWAY,
                status = "Presence unavailable",
                allowSignals = item.allow_invitations,
            )
        }
    }

    override suspend fun syncAll() {
        syncConnections()
        val userId = sessionStore.session.value?.userId ?: return
        val connectionsByUser = remoteConnections.value.associateBy(Connection::id)
        api.invitations(0).invitations.forEach { remote ->
            val incoming = remote.recipient_id == userId
            val otherUserId = if (incoming) remote.sender_id else remote.recipient_id
            val other = connectionsByUser[otherUserId]
            val proposedAt = Instant.parse(remote.proposed_at).toEpochMilli()
            invitationDao.upsert(
                InvitationEntity(
                    id = remote.id,
                    connectionId = otherUserId,
                    connectionName = other?.name ?: "Trusted person",
                    signalId = remote.signal_type.lowercase(),
                    signalTitle = remote.signal_type.lowercase().replaceFirstChar(Char::uppercase) + "?",
                    note = remote.note,
                    timeWindow = if (proposedAt <= System.currentTimeMillis() + 90 * 60_000) "Now" else "Planned",
                    state = remote.state,
                    createdAtEpochMillis = proposedAt,
                    expiresAtEpochMillis = Instant.parse(remote.expires_at).toEpochMilli(),
                    isIncoming = incoming,
                )
            )
        }
    }

    override suspend fun retryPending() {
        invitationDao.pending(System.currentTimeMillis()).forEach { pending ->
            api.createInvitation(
                CreateInvitationRequest(
                    id = pending.id,
                    recipientId = pending.connectionId,
                    signalType = pending.signalId.uppercase(),
                    note = pending.note,
                    proposedAt = Instant.ofEpochMilli(pending.createdAtEpochMillis).toString(),
                    expiresAt = Instant.ofEpochMilli(pending.expiresAtEpochMillis).toString(),
                )
            )
            invitationDao.updateState(pending.id, InvitationState.SENT.name)
        }
    }

    override suspend fun registerPushToken(token: String) {
        api.registerPushToken(com.immrtldragon.detoxspace.data.remote.PushTokenRequest(token))
    }

    override suspend fun createConnectionCode(): String = api.createConnectionInvite().code

    override suspend fun acceptConnectionCode(code: String) {
        api.acceptConnectionInvite(code.trim())
        syncConnections()
    }

    override suspend fun devices(): List<DeviceSession> = api.devices().devices.map {
        DeviceSession(
            id = it.id,
            name = it.device_name,
            lastSeen = runCatching {
                Instant.parse(it.last_seen_at).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM, h:mm a"))
            }.getOrDefault("Recently"),
            isCurrent = it.is_current,
        )
    }

    override suspend fun revokeDevice(id: String): Boolean {
        api.revokeDevice(id)
        val current = sessionStore.current()?.sessionId == id
        if (current) sessionStore.clear()
        return current
    }

    override suspend fun removeConnection(userId: String) {
        api.removeConnection(userId)
        syncConnections()
    }

    override suspend fun blockConnection(userId: String) {
        api.blockConnection(userId)
        syncConnections()
    }

    override suspend fun sendInvitation(connection: Connection, signal: SignalType, window: TimeWindow, note: String?) {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        val expiry = now + window.durationMinutes * 60_000
        invitationDao.upsert(
            InvitationEntity(
                id = id, connectionId = connection.id,
                connectionName = connection.name, signalId = signal.id, signalTitle = signal.title,
                note = note?.trim()?.takeIf(String::isNotEmpty), timeWindow = window.label,
                state = InvitationState.SENDING.name, createdAtEpochMillis = now,
                expiresAtEpochMillis = expiry,
            )
        )
        try {
            api.createInvitation(
                CreateInvitationRequest(
                    id = id,
                    recipientId = connection.id,
                    signalType = signal.id.uppercase(),
                    note = note?.trim()?.takeIf(String::isNotEmpty),
                    proposedAt = Instant.ofEpochMilli(now).toString(),
                    expiresAt = Instant.ofEpochMilli(expiry).toString(),
                )
            )
            invitationDao.updateState(id, InvitationState.SENT.name)
        } catch (_: Exception) {
            // Keep SENDING locally; the same UUID makes every retry idempotent.
            DeliveryWorker.runNow(context)
        }
    }

    override suspend fun updateInvitation(id: String, state: InvitationState) {
        val current = invitationDao.findById(id) ?: return
        val from = InvitationState.valueOf(current.state)
        if (!InvitationPolicy.canTransition(from, state)) return
        when {
            current.isIncoming && state in setOf(InvitationState.ACCEPTED, InvitationState.LATER, InvitationState.DECLINED) ->
                api.respondToInvitation(id, RespondInvitationRequest(state.name))
            !current.isIncoming && state == InvitationState.CANCELLED -> api.cancelInvitation(id)
            state == InvitationState.COMPLETED -> api.completeInvitation(id)
            else -> return
        }
        invitationDao.updateState(id, state.name)
    }
}
