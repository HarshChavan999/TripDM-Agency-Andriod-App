package com.tripdm.agency.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AgencyMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AgencyMessagingService"

        /**
         * Fetch the current device FCM token and register it under the agency's
         * Firestore records so backend Cloud Functions can deliver push notifications
         * when the app is in the background or killed.
         */
        fun registerFCMToken(explicitAgencyId: String? = null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d(TAG, "Fetched FCM registration token: $token")
                saveTokenToFirestore(token, explicitAgencyId)
            }
        }

        fun saveTokenToFirestore(token: String, explicitAgencyId: String? = null) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Log.d(TAG, "User not authenticated yet; FCM token will be saved upon login")
                return
            }
            val agencyId = currentUser.uid
            if (agencyId.isBlank()) return

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    val now = System.currentTimeMillis()

                    // 1. Save to users/{agencyId}
                    firestore.collection("users")
                        .document(agencyId)
                        .set(
                            mapOf(
                                "fcmToken" to token,
                                "fcmTokenUpdatedAt" to now,
                                "updatedAt" to now
                            ),
                            SetOptions.merge()
                        )
                        .await()

                    // 2. Save to fcm_tokens/{agencyId} collection (queried by Cloud Functions)
                    val tokenData = hashMapOf(
                        "userId" to agencyId,
                        "token" to token,
                        "updatedAt" to now,
                        "role" to "agency",
                        "app" to "TripDM-Agency-Android"
                    )
                    firestore.collection("fcm_tokens")
                        .document(agencyId)
                        .set(tokenData, SetOptions.merge())
                        .await()

                    Log.d(TAG, "FCM token saved successfully for agency: $agencyId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving FCM token to Firestore: ${e.message}", e)
                }
            }
        }

        fun removeToken(agencyId: String) {
            if (agencyId.isBlank()) return
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val firestore = FirebaseFirestore.getInstance()
                    firestore.collection("users")
                        .document(agencyId)
                        .update("fcmToken", null)
                        .await()

                    firestore.collection("fcm_tokens")
                        .document(agencyId)
                        .delete()
                        .await()

                    Log.d(TAG, "FCM token removed successfully for agency: $agencyId")
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing FCM token from Firestore: ${e.message}", e)
                }
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token generated: $token")
        saveTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val senderName = data["senderName"]
            ?: data["sender_name"]
            ?: data["title"]
            ?: remoteMessage.notification?.title
            ?: "New Traveler Inquiry"

        val messageText = data["message"]
            ?: data["message_text"]
            ?: data["body"]
            ?: data["content"]
            ?: remoteMessage.notification?.body
            ?: "You received a new inquiry from a traveler."

        val senderId = data["senderId"]
            ?: data["from_user_id"]
            ?: data["from"]

        AgencyNotificationHelper.createNotificationChannel(applicationContext)
        AgencyNotificationHelper.showNotification(
            context = applicationContext,
            title = if (senderName.startsWith("New Lead") || senderName.startsWith("New Inquiry")) {
                senderName
            } else {
                "New Lead: $senderName"
            },
            message = messageText,
            senderId = senderId
        )
    }
}
