package com.immrtldragon.detoxspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.immrtldragon.detoxspace.data.DetoxRepository
import com.immrtldragon.detoxspace.data.SettingsRepository
import com.immrtldragon.detoxspace.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetoxViewModel @Inject constructor(
    private val repository: DetoxRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    init { viewModelScope.launch { runCatching { repository.syncConnections() } } }
    val connections = repository.connections.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recentSignals = repository.invitations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val presence = repository.presence.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Presence.AVAILABLE)
    val darkMode = settings.darkMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val signalTypes = listOf(
        SignalType("walk", "🚶", "Walk?", "Step outside together"),
        SignalType("coffee", "☕", "Coffee?", "A small break, offline"),
        SignalType("talk", "💬", "Talk?", "I have time to listen"),
        SignalType("free", "✨", "Free?", "Want to spend some time?"),
    )

    fun setPresence(value: Presence) = repository.setPresence(value)
    fun setDarkMode(enabled: Boolean) = viewModelScope.launch { settings.setDarkMode(enabled) }
    fun sendSignal(connection: Connection, signal: SignalType, window: TimeWindow, note: String?) =
        viewModelScope.launch { repository.sendInvitation(connection, signal, window, note) }
    fun updateInvitation(id: String, state: InvitationState) = viewModelScope.launch {
        repository.updateInvitation(id, state)
    }
}
