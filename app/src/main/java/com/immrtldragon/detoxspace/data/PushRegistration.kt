package com.immrtldragon.detoxspace.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRegistration @Inject constructor(
    private val sessionStore: SessionStore,
    private val repository: DetoxRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            sessionStore.session.filterNotNull().collect {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    scope.launch { runCatching { repository.registerPushToken(token) } }
                }
            }
        }
    }
}
