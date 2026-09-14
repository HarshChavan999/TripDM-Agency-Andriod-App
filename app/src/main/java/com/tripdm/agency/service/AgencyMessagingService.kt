package com.tripdm.agency.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AgencyMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "New Customer Inquiry"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "A traveler has reached out regarding a package."

        AgencyNotificationHelper.showNotification(applicationContext, title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Store FCM token in Firestore under agency profile if needed
    }
}
