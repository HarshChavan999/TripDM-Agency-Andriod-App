package com.tripdm.agency.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.model.ChatMessage
import com.tripdm.agency.data.model.ChatMessageStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AgencyChatRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeConversations(agencyId: String): Flow<List<ChatConversation>> = callbackFlow {
        // Listen to messages where agency is sender or recipient
        val listener = firestore.collection("chat_messages")
            .whereEqualTo("to_user_id", agencyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val groupedByUser = mutableMapOf<String, ChatConversation>()
                    for (doc in snapshot.documents) {
                        val senderId = doc.getString("from_user_id") ?: doc.getString("from") ?: ""
                        if (senderId.isEmpty()) continue
                        val text = doc.getString("message_text") ?: doc.getString("content") ?: ""
                        val ts = doc.getTimestamp("created_at")?.toDate()?.time
                            ?: doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val senderName = doc.getString("sender_name") ?: doc.getString("userName") ?: "Traveler"
                        val senderEmail = doc.getString("sender_email") ?: ""
                        val listingTitle = doc.getString("listing_title") ?: doc.getString("listingTitle")

                        val existing = groupedByUser[senderId]
                        if (existing == null || ts > existing.lastMessageTimestamp) {
                            groupedByUser[senderId] = ChatConversation(
                                otherUserId = senderId,
                                otherUserName = senderName,
                                otherUserEmail = senderEmail,
                                lastMessage = text,
                                lastMessageTimestamp = ts,
                                unreadCount = if (doc.getBoolean("is_read") == false) 1 else 0,
                                relatedListingTitle = listingTitle
                            )
                        }
                    }
                    trySend(groupedByUser.values.sortedByDescending { it.lastMessageTimestamp })
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeMessages(agencyId: String, otherUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = firestore.collection("chat_messages")
            .whereIn("from_user_id", listOf(agencyId, otherUserId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val from = doc.getString("from_user_id") ?: doc.getString("from") ?: ""
                        val to = doc.getString("to_user_id") ?: doc.getString("to") ?: ""
                        if (!((from == agencyId && to == otherUserId) || (from == otherUserId && to == agencyId))) {
                            return@mapNotNull null
                        }
                        val text = doc.getString("message_text") ?: doc.getString("content") ?: ""
                        val ts = doc.getTimestamp("created_at")?.toDate()?.time
                            ?: doc.getLong("timestamp") ?: System.currentTimeMillis()

                        ChatMessage(
                            id = doc.id,
                            from = from,
                            to = to,
                            content = text,
                            timestamp = ts,
                            status = if (doc.getBoolean("is_read") == true) ChatMessageStatus.READ else ChatMessageStatus.SENT,
                            listingTitle = doc.getString("listing_title") ?: doc.getString("listingTitle")
                        )
                    }.sortedBy { it.timestamp }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        agencyId: String,
        agencyName: String,
        targetUserId: String,
        content: String,
        listingTitle: String? = null
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection("chat_messages").document()
            val data = hashMapOf(
                "id" to docRef.id,
                "from_user_id" to agencyId,
                "to_user_id" to targetUserId,
                "sender_name" to agencyName,
                "message_text" to content,
                "content" to content,
                "from" to agencyId,
                "to" to targetUserId,
                "created_at" to com.google.firebase.Timestamp.now(),
                "timestamp" to System.currentTimeMillis(),
                "is_read" to false,
                "listing_title" to listingTitle
            )
            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
