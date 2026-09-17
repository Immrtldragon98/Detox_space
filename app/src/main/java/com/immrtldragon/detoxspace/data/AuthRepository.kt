package com.immrtldragon.detoxspace.data

import android.os.Build
import com.immrtldragon.detoxspace.data.remote.DetoxApi
import com.immrtldragon.detoxspace.data.remote.LoginRequest
import com.immrtldragon.detoxspace.data.remote.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(private val api: DetoxApi, private val sessions: SessionStore) {
    val session = sessions.session

    suspend fun login(login: String, password: String) {
        val result = api.login(LoginRequest(login.trim(), password, deviceName()))
        sessions.save(Session(result.accessToken, result.refreshToken, result.userId, result.sessionId))
    }

    suspend fun register(username: String, email: String, password: String) {
        val result = api.register(RegisterRequest(username.trim(), email.trim(), password, deviceName()))
        sessions.save(Session(result.accessToken, result.refreshToken, result.userId, result.sessionId))
    }

    suspend fun logout() {
        runCatching { api.logout() }
        sessions.clear()
    }
    suspend fun deleteAccount() {
        api.deleteAccount()
        sessions.clear()
    }
    private fun deviceName() = "${Build.MANUFACTURER} ${Build.MODEL}".trim().take(80)
}
