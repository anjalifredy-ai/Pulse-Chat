package com.pulsechat.app.service

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pulsechat.app.PulseChatApp
import com.pulsechat.app.R
import com.pulsechat.app.ui.MainActivity
import com.pulsechat.app.ui.call.IncomingCallActivity

class PulseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // Token should be saved to Firestore under the current user
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when (data["type"]) {
            "call" -> handleIncomingCall(data)
            "message" -> handleMessageNotification(data)
            else -> {
                message.notification?.let {
                    showSimpleNotification(it.title ?: "Pulse Chat", it.body ?: "")
                }
            }
        }
    }

    private fun handleIncomingCall(data: Map<String, String>) {
        val callId = data["callId"] ?: return
        val callerName = data["callerName"] ?: "Unknown"
        val callType = data["callType"] ?: "voice"

        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("callId", callId)
            putExtra("callerName", callerName)
            putExtra("callType", callType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPending = PendingIntent.getActivity(
            this, callId.hashCode(), fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, PulseChatApp.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(if (callType == "video") "Incoming video call" else "Incoming voice call")
            .setContentText(callerName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPending, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(callId.hashCode(), notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    private fun handleMessageNotification(data: Map<String, String>) {
        val title = data["title"] ?: "New message"
        val body = data["body"] ?: ""
        val conversationId = data["conversationId"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (conversationId != null) putExtra("conversationId", conversationId)
        }
        val pending = PendingIntent.getActivity(
            this, conversationId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, PulseChatApp.CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(this)
                .notify((conversationId ?: title).hashCode(), notification)
        } catch (_: SecurityException) {
        }
    }

    private fun showSimpleNotification(title: String, body: String) {
        val notification = NotificationCompat.Builder(this, PulseChatApp.CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
        } catch (_: SecurityException) {
        }
    }
}
