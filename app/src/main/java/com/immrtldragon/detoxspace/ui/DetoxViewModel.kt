package com.immrtldragon.detoxspace.ui

import androidx.lifecycle.ViewModel
import com.immrtldragon.detoxspace.data.DetoxRepository
import com.immrtldragon.detoxspace.data.InMemoryDetoxRepository
import com.immrtldragon.detoxspace.domain.Connection
import com.immrtldragon.detoxspace.domain.Presence
import com.immrtldragon.detoxspace.domain.SignalType

class DetoxViewModel(
    private val repository: DetoxRepository = InMemoryDetoxRepository(),
) : ViewModel() {
    val connections = repository.connections
    val recentSignals = repository.recentSignals
    val presence = repository.presence

    val signalTypes = listOf(
        SignalType("walk", "🚶", "Walk?", "Step outside together"),
        SignalType("coffee", "☕", "Coffee?", "A small break, offline"),
        SignalType("talk", "💬", "Talk?", "I have time to listen"),
        SignalType("free", "✨", "Free?", "Want to spend some time?"),
    )

    fun setPresence(value: Presence) = repository.setPresence(value)
    fun sendSignal(connection: Connection, signal: SignalType) = repository.sendSignal(connection, signal)
}

