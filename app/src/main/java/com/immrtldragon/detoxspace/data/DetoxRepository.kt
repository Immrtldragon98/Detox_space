package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.data.local.InvitationDao
import com.immrtldragon.detoxspace.data.local.InvitationEntity
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
    suspend fun sendInvitation(connection: Connection, signal: SignalType, window: TimeWindow, note: String?)
    suspend fun updateInvitation(id: String, state: InvitationState)
}

@Singleton
class OfflineFirstDetoxRepository @Inject constructor(private val invitationDao: InvitationDao) : DetoxRepository {
    private val localPresence = MutableStateFlow(Presence.AVAILABLE)
    override val presence: Flow<Presence> = localPresence
    override val connections: Flow<List<Connection>> = MutableStateFlow(
        listOf(
            Connection("1", "Maya", "MA", Presence.AVAILABLE, "Free for a walk"),
            Connection("2", "Arun", "AR", Presence.QUIET, "Taking a quiet evening"),
            Connection("3", "Nila", "NI", Presence.AWAY, "Back later"),
        )
    )
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

    override suspend fun sendInvitation(connection: Connection, signal: SignalType, window: TimeWindow, note: String?) {
        val now = System.currentTimeMillis()
        invitationDao.upsert(
            InvitationEntity(
                id = UUID.randomUUID().toString(), connectionId = connection.id,
                connectionName = connection.name, signalId = signal.id, signalTitle = signal.title,
                note = note?.trim()?.takeIf(String::isNotEmpty), timeWindow = window.label,
                state = InvitationState.SENT.name, createdAtEpochMillis = now,
                expiresAtEpochMillis = now + window.durationMinutes * 60_000,
            )
        )
    }

    override suspend fun updateInvitation(id: String, state: InvitationState) {
        val current = invitationDao.findById(id) ?: return
        val from = InvitationState.valueOf(current.state)
        if (InvitationPolicy.canTransition(from, state)) invitationDao.updateState(id, state.name)
    }
}
