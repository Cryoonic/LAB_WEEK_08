package com.example.lab_week_08

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class SecondNotificationService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "Unknown"
        Log.d("SecondNotificationService", "Service started for ID: $id")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Second Foreground Service")
            .setContentText("Second Notification Service for ID $id is running...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(2, notification)

        // Countdown tetap 10 detik
        Thread {
            try {
                for (i in 10 downTo 1) {
                    Log.d("SecondNotificationService", "Second Notification ends in $i seconds")
                    Thread.sleep(1000)
                }
            } finally {
                stopForeground(true)
                stopSelf()
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Second Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "Second_Notification_Channel"
        const val EXTRA_ID = "SecondService_Id"
    }
}
