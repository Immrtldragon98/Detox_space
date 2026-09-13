package com.immrtldragon.detoxspace.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Session(val accessToken: String, val refreshToken: String, val userId: String, val sessionId: String)

@Singleton
class SessionStore @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "detox_secure_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val _session = MutableStateFlow(read())
    val session: StateFlow<Session?> = _session
    fun accessToken(): String? = _session.value?.accessToken

    fun save(value: Session) {
        prefs.edit().putString("access", value.accessToken).putString("refresh", value.refreshToken)
            .putString("user", value.userId).putString("device_session", value.sessionId).apply()
        _session.value = value
    }

    fun clear() { prefs.edit().clear().apply(); _session.value = null }

    private fun read(): Session? {
        val access = prefs.getString("access", null) ?: return null
        val refresh = prefs.getString("refresh", null) ?: return null
        val user = prefs.getString("user", null) ?: return null
        val deviceSession = prefs.getString("device_session", null) ?: return null
        return Session(access, refresh, user, deviceSession)
    }
}
