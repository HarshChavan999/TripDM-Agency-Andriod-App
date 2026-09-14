package com.tripdm.agency.data.model

data class ChatMessage(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: ChatMessageStatus = ChatMessageStatus.SENT,
    val listingId: String? = null,
    val listingTitle: String? = null,
    val senderName: String? = null,
    val replyToId: String? = null,
    val replyToContent: String? = null
)

enum class ChatMessageStatus {
    SENT,
    DELIVERED,
    READ
}

data class ChatConversation(
    val otherUserId: String,
    val otherUserName: String,
    val otherUserEmail: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val relatedListingTitle: String? = null
)
