package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.domain.Connection
import com.immrtldragon.detoxspace.domain.Presence
import com.immrtldragon.detoxspace.domain.SentSignal
import com.immrtldragon.detoxspace.domain.SignalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

interface DetoxRepository {
    val connections: StateFlow<List<Connection>>
    val recentSignals: StateFlow<List<SentSignal>>
    val presence: StateFlow<Presence>
    fun setPresence(value: Presence)
    fun sendSignal(connection: Connection, signal: SignalType)
}

class InMemoryDetoxRepository : DetoxRepository {
    private val _connections = MutableStateFlow(
        listOf(
            Connection("1", "Maya", "MA", Presence.AVAILABLE, "Free for a walk"),
            Connection("2", "Arun", "AR", Presence.QUIET, "Taking a quiet evening"),
            Connection("3", "Nila", "NI", Presence.AWAY, "Back later"),
        )
    )
    override val connections = _connections.asStateFlow()

    private val _recentSignals = MutableStateFlow<List<SentSignal>>(emptyList())
    override val recentSignals = _recentSignals.asStateFlow()

    private val _presence = MutableStateFlow(Presence.AVAILABLE)
    override val presence = _presence.asStateFlow()

    override fun setPresence(value: Presence) { _presence.value = value }

    override fun sendSignal(connection: Connection, signal: SignalType) {
        val item = SentSignal(
            id = UUID.randomUUID().toString(),
            connectionName = connection.name,
            signalTitle = signal.title,
            sentAt = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a")),
        )
        _recentSignals.value = listOf(item) + _recentSignals.value.take(4)
    }
}

