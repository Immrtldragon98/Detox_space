package com.immrtldragon.detoxspace.data

import com.immrtldragon.detoxspace.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeClient @Inject constructor(
    private val sessionStore: SessionStore,
    private val repository: DetoxRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var socket: Socket? = null

    fun start() {
        scope.launch {
            sessionStore.session.collectLatest { session ->
                socket?.disconnect()
                socket = null
                if (session == null) return@collectLatest
                val options = IO.Options.builder()
                    .setAuth(mapOf("token" to session.accessToken))
                    .setReconnection(true)
                    .build()
                socket = IO.socket(BuildConfig.API_BASE_URL.removeSuffix("/"), options).apply {
                    on("sync_required") { scope.launch { runCatching { repository.syncAll() } } }
                    connect()
                }
            }
        }
    }
}
