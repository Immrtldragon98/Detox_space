package com.immrtldragon.detoxspace.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.immrtldragon.detoxspace.MainActivity
import com.immrtldragon.detoxspace.R
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
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        manager.notify(
            message.messageId?.hashCode() ?: System.currentTimeMillis().toInt(),
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Detox Space")
                .setContentText("A moment is waiting for you.")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build(),
        )
    }

    private companion object { const val CHANNEL_ID = "private_moments" }
}
