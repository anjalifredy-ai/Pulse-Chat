package com.pulsechat.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PulseChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val messages = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                enableVibration(true)
            }

            val calls = NotificationChannel(
                CHANNEL_CALLS,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming and ongoing call notifications"
                setSound(null, null)
                enableVibration(true)
            }

            val status = NotificationChannel(
                CHANNEL_STATUS,
                "Status updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Status and story notifications"
            }

            val general = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_LOW
            )

            manager.createNotificationChannels(listOf(messages, calls, status, general))
        }
    }

    companion object {
        const val CHANNEL_MESSAGES = "messages"
        const val CHANNEL_CALLS = "calls"
        const val CHANNEL_STATUS = "status"
        const val CHANNEL_GENERAL = "general"
    }
}
