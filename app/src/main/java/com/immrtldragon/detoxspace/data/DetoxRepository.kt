package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.data.local.InvitationDao
import com.immrtldragon.detoxspace.data.local.InvitationEntity
import com.immrtldragon.detoxspace.data.remote.CreateInvitationRequest
import com.immrtldragon.detoxspace.data.remote.DetoxApi
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
    suspend fun sendInvitation(connection: Connection, signal: SignalType, window: TimeWindow, note: String?)
    suspend fun updateInvitation(id: String, state: InvitationState)
}

@Singleton
class OfflineFirstDetoxRepository @Inject constructor(
    private val invitationDao: InvitationDao,
    private val api: DetoxApi,
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
            // Keep SENDING locally. WorkManager will retry this idempotent UUID in the sync milestone.
        }
    }

    override suspend fun updateInvitation(id: String, state: InvitationState) {
        val current = invitationDao.findById(id) ?: return
        val from = InvitationState.valueOf(current.state)
        if (InvitationPolicy.canTransition(from, state)) invitationDao.updateState(id, state.name)
    }
}
