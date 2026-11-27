package com.immrtldragon.detoxspace.data

import android.content.Context
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.immrtldragon.detoxspace.MainActivity
import com.immrtldragon.detoxspace.R
import com.immrtldragon.detoxspace.domain.InvitationState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DeliveryWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: DetoxRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        val before = repository.invitations.first().associate { it.id to it.state }
        repository.retryPending()
        repository.syncAll()
        val changed = repository.invitations.first().any {
            it.isIncoming && it.state in setOf(InvitationState.SENT, InvitationState.LATER, InvitationState.ACCEPTED) &&
                before[it.id] != it.state
        }
        if (changed && NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
            val openApp = PendingIntent.getActivity(applicationContext, 0,
                Intent(applicationContext, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            applicationContext.getSystemService(NotificationManager::class.java).notify(1001,
                NotificationCompat.Builder(applicationContext, "private_moments")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Detox Space")
                    .setContentText("A moment is waiting for you.")
                    .setContentIntent(openApp).setAutoCancel(true).build())
        }
    }.fold(onSuccess = { Result.success() }, onFailure = {
        if (it is CancellationException) throw it
        Result.retry()
    })

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DeliveryWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "detox-delivery-sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<DeliveryWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
