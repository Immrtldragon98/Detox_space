package com.immrtldragon.detoxspace

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.immrtldragon.detoxspace.data.DeliveryWorker
import com.immrtldragon.detoxspace.data.RealtimeClient
import com.immrtldragon.detoxspace.data.PushRegistration
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DetoxSpaceApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var realtimeClient: RealtimeClient
    @Inject lateinit var pushRegistration: PushRegistration

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("private_moments", "Private moments", NotificationManager.IMPORTANCE_DEFAULT),
        )
        DeliveryWorker.schedule(this)
        realtimeClient.start()
        if (FirebaseApp.getApps(this).isNotEmpty()) pushRegistration.start()
    }
}
