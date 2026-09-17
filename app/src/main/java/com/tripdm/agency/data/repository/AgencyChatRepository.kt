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
        val trimmedAgencyId = agencyId.trim()
        val listener = firestore.collection("chat_messages")
            .whereEqualTo("to_user_id", trimmedAgencyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    data class UserLeadData(
                        val senderId: String,
                        var rawName: String,
                        var email: String,
                        var lastMessage: String,
                        var lastTimestamp: Long,
                        var firstTimestamp: Long,
                        var unreadCount: Int,
                        var listingTitle: String?
                    )

                    val customerMap = mutableMapOf<String, UserLeadData>()

                    for (doc in snapshot.documents) {
                        val senderId = doc.getString("from_user_id") ?: doc.getString("from") ?: ""
                        if (senderId.isEmpty() || senderId == trimmedAgencyId) continue
                        val text = doc.getString("message_text") ?: doc.getString("content") ?: ""
                        val ts = doc.getTimestamp("created_at")?.toDate()?.time
                            ?: doc.getLong("timestamp") ?: System.currentTimeMillis()
                        val senderName = doc.getString("sender_name") ?: doc.getString("userName") ?: ""
                        val senderEmail = doc.getString("sender_email") ?: ""
                        val listingTitle = doc.getString("listing_title") ?: doc.getString("listingTitle")
                        val isUnread = doc.getBoolean("is_read") == false || doc.getString("status") == "sent"

                        val existing = customerMap[senderId]
                        if (existing == null) {
                            customerMap[senderId] = UserLeadData(
                                senderId = senderId,
                                rawName = senderName,
                                email = senderEmail,
                                lastMessage = text,
                                lastTimestamp = ts,
                                firstTimestamp = ts,
                                unreadCount = if (isUnread) 1 else 0,
                                listingTitle = listingTitle
                            )
                        } else {
                            if (ts > existing.lastTimestamp) {
                                existing.lastMessage = text
                                existing.lastTimestamp = ts
                                if (!listingTitle.isNullOrBlank()) existing.listingTitle = listingTitle
                                if (senderName.isNotBlank() && !senderName.startsWith("Traveler", ignoreCase = true)) {
                                    existing.rawName = senderName
                                }
                            }
                            if (ts < existing.firstTimestamp) {
                                existing.firstTimestamp = ts
                            }
                            if (isUnread) {
                                existing.unreadCount += 1
                            }
                        }
                    }

                    // Order unique customers by their earliest contact timestamp ascending
                    val orderedByFirstContact = customerMap.values.sortedBy { it.firstTimestamp }
                    val leadSeqMap = mutableMapOf<String, Int>()
                    orderedByFirstContact.forEachIndexed { index, data ->
                        leadSeqMap[data.senderId] = index + 1
                    }

                    // Map to ChatConversation with Lead #1, Lead #2... when name is missing or "Traveler"
                    val conversationList = customerMap.values.map { data ->
                        val seqNum = leadSeqMap[data.senderId] ?: 1
                        val displayName = if (data.rawName.isBlank() ||
                            data.rawName.startsWith("Traveler", ignoreCase = true) ||
                            data.rawName.startsWith("New Traveler", ignoreCase = true)
                        ) {
                            "Lead #$seqNum"
                        } else {
                            data.rawName
                        }

                        ChatConversation(
                            otherUserId = data.senderId,
                            otherUserName = displayName,
                            otherUserEmail = data.email,
                            lastMessage = data.lastMessage,
                            lastMessageTimestamp = data.lastTimestamp,
                            unreadCount = data.unreadCount,
                            relatedListingTitle = data.listingTitle
                        )
                    }.sortedByDescending { it.lastMessageTimestamp }

                    trySend(conversationList)
                }
            }
        awaitClose { listener.remove() }
    }

    fun listenForNewLeads(
        agencyId: String,
        onNewLead: (senderName: String, messageText: String, senderId: String) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        val trimmedAgencyId = agencyId.trim()
        var isInitialSnapshot = true

        return firestore.collection("chat_messages")
            .whereEqualTo("to_user_id", trimmedAgencyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // Skip initial snapshot so existing messages don't trigger alerts
                if (isInitialSnapshot) {
                    isInitialSnapshot = false
                    return@addSnapshotListener
                }

                for (change in snapshot.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val senderId = doc.getString("from_user_id") ?: doc.getString("from") ?: ""
                        if (senderId.isNotBlank() && senderId != trimmedAgencyId) {
                            val text = doc.getString("message_text")
                                ?: doc.getString("content")
                                ?: "New inquiry received"
                            val rawName = doc.getString("sender_name") ?: doc.getString("userName")
                            val senderName = if (rawName.isNullOrBlank() || rawName.startsWith("Traveler", ignoreCase = true)) {
                                "New Lead"
                            } else {
                                rawName
                            }
                            onNewLead(senderName, text, senderId)
                        }
                    }
                }
            }
    }

    fun observeMessages(agencyId: String, otherUserId: String): Flow<List<ChatMessage>> = callbackFlow {
        val trimmedAgencyId = agencyId.trim()
        val listener = firestore.collection("chat_messages")
            .whereIn("from_user_id", listOf(trimmedAgencyId, otherUserId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val from = doc.getString("from_user_id") ?: doc.getString("from") ?: ""
                        val to = doc.getString("to_user_id") ?: doc.getString("to") ?: ""
                        if (!((from == trimmedAgencyId && to == otherUserId) || (from == otherUserId && to == trimmedAgencyId))) {
                            return@mapNotNull null
                        }
                        val text = doc.getString("message_text") ?: doc.getString("content") ?: ""
                        val ts = doc.getTimestamp("created_at")?.toDate()?.time
                            ?: doc.getLong("timestamp") ?: System.currentTimeMillis()

                        val isRead = doc.getBoolean("is_read") == true || doc.getString("status") == "read"
                        val isEdited = doc.getBoolean("is_edited") ?: doc.getBoolean("isEdited") ?: false
                        
                        @Suppress("UNCHECKED_CAST")
                        val reactions = (doc.get("reactions") as? Map<String, String>) ?: emptyMap()

                        ChatMessage(
                            id = doc.id,
                            from = from,
                            to = to,
                            content = text,
                            timestamp = ts,
                            status = if (isRead) ChatMessageStatus.READ else ChatMessageStatus.SENT,
                            isEdited = isEdited,
                            reactions = reactions,
                            listingTitle = doc.getString("listing_title") ?: doc.getString("listingTitle"),
                            senderName = doc.getString("sender_name") ?: doc.getString("userName"),
                            replyToId = doc.getString("reply_to_id") ?: doc.getString("replyToId"),
                            replyToContent = doc.getString("reply_to_content") ?: doc.getString("replyToContent"),
                            replyToSenderName = doc.getString("reply_to_sender_name") ?: doc.getString("replyToSenderName")
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
        listingTitle: String? = null,
        replyToId: String? = null,
        replyToContent: String? = null,
        replyToSenderName: String? = null
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection("chat_messages").document()
            val now = System.currentTimeMillis()
            val data = hashMapOf<String, Any?>(
                "id" to docRef.id,
                "from_user_id" to agencyId.trim(),
                "to_user_id" to targetUserId.trim(),
                "sender_name" to agencyName,
                "message_text" to content,
                "content" to content,
                "from" to agencyId.trim(),
                "to" to targetUserId.trim(),
                "created_at" to com.google.firebase.Timestamp.now(),
                "timestamp" to now,
                "status" to "sent",
                "is_read" to false,
                "listing_title" to listingTitle,
                "reply_to_id" to replyToId,
                "reply_to_content" to replyToContent,
                "reply_to_sender_name" to replyToSenderName
            )
            docRef.set(data.filterValues { it != null }).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            firestore.collection("chat_messages").document(messageId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editMessage(messageId: String, newContent: String): Result<Unit> {
        return try {
            firestore.collection("chat_messages").document(messageId).update(
                mapOf(
                    "message_text" to newContent,
                    "content" to newContent,
                    "is_edited" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reactToMessage(messageId: String, agencyId: String, emoji: String): Result<Unit> {
        return try {
            val docRef = firestore.collection("chat_messages").document(messageId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                @Suppress("UNCHECKED_CAST")
                val currentReactions = (snapshot.get("reactions") as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()
                if (currentReactions[agencyId] == emoji) {
                    currentReactions.remove(agencyId)
                } else {
                    currentReactions[agencyId] = emoji
                }
                transaction.update(docRef, "reactions", currentReactions)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

