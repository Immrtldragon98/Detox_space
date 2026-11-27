package com.immrtldragon.detoxspace.data

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetoxMessagingService : FirebaseMessagingService() {
    @Inject lateinit var repository: DetoxRepository
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { runCatching { repository.registerPushToken(token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data["type"] == "sync_required") DeliveryWorker.runNow(this)
    }
}
