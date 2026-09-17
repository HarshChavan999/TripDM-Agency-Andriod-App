package com.tripdm.agency.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tripdm.agency.AgencyMainActivity
import com.tripdm.agency.R
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

object AgencyNotificationHelper {
    const val CHANNEL_ID = "tripdm_agency_leads"
    const val CHANNEL_NAME = "TripDM Traveler Leads & Inquiries"
    private const val TAG = "AgencyNotificationHelper"

    // Tracks currently open chat conversation to prevent notification banners while chatting
    var activeChatUserId: String? = null

    // Cache of recent notifications to deduplicate within a 5-second window
    private val recentNotifications = ConcurrentHashMap<String, Long>()

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time notifications for incoming traveler leads and chat inquiries"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 200, 350)
                enableLights(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        senderId: String? = null,
        isNewLead: Boolean = false,
        customNotificationId: Int? = null
    ) {
        // 1. If the user is actively in a chat with this sender, suppress notification
        if (senderId != null && senderId == activeChatUserId) {
            Log.d(TAG, "Suppressed notification for active chat user: $senderId")
            return
        }

        // 2. Deduplicate within 5 seconds for the same sender and message
        val dedupKey = "${senderId ?: "unknown"}:${message.trim()}"
        val now = System.currentTimeMillis()
        val lastShown = recentNotifications[dedupKey]
        if (lastShown != null && (now - lastShown) < 5000L) {
            Log.d(TAG, "Deduplicated repeated notification for key: $dedupKey")
            return
        }
        recentNotifications[dedupKey] = now

        // Clean up old entries
        if (recentNotifications.size > 40) {
            recentNotifications.entries.removeIf { now - it.value > 30000L }
        }

        createNotificationChannel(context)

        // 3. Use stable notification ID per sender so subsequent messages update the thread
        val notificationId = customNotificationId
            ?: (senderId?.let { abs(it.hashCode()) % 90000 + 1000 } ?: 1001)

        val intent = Intent(context, AgencyMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (senderId != null) {
                putExtra("OPEN_CHAT_USER_ID", senderId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summary = if (isNewLead) "New Traveler Lead" else "Chat Message"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message).setSummaryText(summary))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 350, 200, 350))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
        Log.d(TAG, "Notification displayed successfully (id: $notificationId, sender: $senderId, isNewLead: $isNewLead)")
    }
}
