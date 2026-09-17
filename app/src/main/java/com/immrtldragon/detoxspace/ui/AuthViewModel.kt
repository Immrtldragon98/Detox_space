package com.immrtldragon.detoxspace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.immrtldragon.detoxspace.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

data class AuthUiState(val loading: Boolean = false, val error: String? = null)

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    val session = repository.session
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    fun login(login: String, password: String) = submit { repository.login(login, password) }
    fun register(username: String, email: String, password: String) = submit {
        repository.register(username, email, password)
    }
    fun logout() = viewModelScope.launch { repository.logout() }
    fun deleteAccount() = submit { repository.deleteAccount() }
    fun clearError() { _ui.value = _ui.value.copy(error = null) }

    private fun submit(block: suspend () -> Unit) = viewModelScope.launch {
        _ui.value = AuthUiState(loading = true)
        _ui.value = try { block(); AuthUiState() } catch (error: Exception) {
            AuthUiState(error = when (error) {
                is IOException -> "Cannot reach Detox Space. Check your connection."
                is HttpException -> if (error.code() == 401) "Incorrect username/email or password." else "Request failed (${error.code()})."
                else -> "Something went wrong. Please try again."
            })
        }
    }
}
