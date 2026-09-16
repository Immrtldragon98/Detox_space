package com.immrtldragon.detoxspace.data

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
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
        scope.launch { runCatching { repository.syncAll() } }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Private moments", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.notify(
            message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(applicationInfo.icon)
                .setContentTitle("Detox Space")
                .setContentText("A moment is waiting for you.")
                .setAutoCancel(true)
                .build(),
        )
    }

    private companion object { const val CHANNEL_ID = "private_moments" }
}
