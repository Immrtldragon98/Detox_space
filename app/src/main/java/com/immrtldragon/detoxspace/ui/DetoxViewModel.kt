package com.immrtldragon.detoxspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.immrtldragon.detoxspace.data.DetoxRepository
import com.immrtldragon.detoxspace.data.SettingsRepository
import com.immrtldragon.detoxspace.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetoxViewModel @Inject constructor(
    private val repository: DetoxRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    data class ConnectionCodeUiState(
        val loading: Boolean = false,
        val generatedCode: String? = null,
        val message: String? = null,
        val error: String? = null,
    )

    private val _connectionCodeUi = MutableStateFlow(ConnectionCodeUiState())
    val connectionCodeUi = _connectionCodeUi.asStateFlow()
    private val _syncing = MutableStateFlow(false)
    val syncing = _syncing.asStateFlow()
    init { refresh() }
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
        runCatching { repository.updateInvitation(id, state) }
    }

    fun refresh() = viewModelScope.launch {
        _syncing.value = true
        runCatching { repository.syncAll() }
        _syncing.value = false
    }

    fun createConnectionCode() = viewModelScope.launch {
        _connectionCodeUi.update { it.copy(loading = true, message = null, error = null) }
        runCatching { repository.createConnectionCode() }
            .onSuccess { code -> _connectionCodeUi.value = ConnectionCodeUiState(generatedCode = code) }
            .onFailure { _connectionCodeUi.value = ConnectionCodeUiState(error = connectionError(it)) }
    }

    fun acceptConnectionCode(code: String) = viewModelScope.launch {
        _connectionCodeUi.update { it.copy(loading = true, message = null, error = null) }
        runCatching { repository.acceptConnectionCode(code) }
            .onSuccess { _connectionCodeUi.value = ConnectionCodeUiState(message = "You are now connected.") }
            .onFailure { _connectionCodeUi.value = ConnectionCodeUiState(error = connectionError(it)) }
    }

    fun clearConnectionCodeState() { _connectionCodeUi.value = ConnectionCodeUiState() }

    private fun connectionError(error: Throwable): String {
        val body = (error as? retrofit2.HttpException)?.response()?.errorBody()?.string().orEmpty()
        return when {
            "invite_unavailable" in body -> "That code is expired, used, or invalid. Ask for a new one."
            "cannot_connect_to_self" in body -> "Use a code created by another person."
            else -> "Could not connect right now. Check your internet and try again."
        }
    }
}
