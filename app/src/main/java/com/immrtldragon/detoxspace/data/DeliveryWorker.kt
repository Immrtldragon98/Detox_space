package com.immrtldragon.detoxspace.data

import android.content.Context
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
        repository.retryPending()
        repository.syncAll()
    }.fold(onSuccess = { Result.success() }, onFailure = { Result.retry() })

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
