package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.model.ChatMessage
import com.tripdm.agency.data.repository.AgencyChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AgencyChatViewModel(
    private val repository: AgencyChatRepository = AgencyChatRepository()
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations.asStateFlow()

    private val _activeConversation = MutableStateFlow<ChatConversation?>(null)
    val activeConversation: StateFlow<ChatConversation?> = _activeConversation.asStateFlow()

    private val _activeMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessage>> = _activeMessages.asStateFlow()

    private val messagesCache = mutableMapOf<String, List<ChatMessage>>()

    fun loadConversations(agencyId: String) {
        viewModelScope.launch {
            repository.observeConversations(agencyId).collect { list ->
                val activeUserId = _activeConversation.value?.otherUserId
                val updatedList = if (activeUserId != null) {
                    list.map { conv ->
                        if (conv.otherUserId == activeUserId) conv.copy(unreadCount = 0) else conv
                    }
                } else list
                _conversations.value = updatedList
            }
        }
    }

    fun openConversation(agencyId: String, conversation: ChatConversation) {
        _activeConversation.value = conversation.copy(unreadCount = 0)
        _conversations.value = _conversations.value.map { conv ->
            if (conv.otherUserId == conversation.otherUserId) conv.copy(unreadCount = 0) else conv
        }

        // Preload messages instantly from in-memory cache if available
        val cached = messagesCache[conversation.otherUserId]
        if (cached != null) {
            _activeMessages.value = cached
        } else {
            _activeMessages.value = emptyList()
        }

        viewModelScope.launch {
            repository.markMessagesAsRead(agencyId, conversation.otherUserId)
        }

        viewModelScope.launch {
            repository.observeMessages(agencyId, conversation.otherUserId).collect { msgs ->
                messagesCache[conversation.otherUserId] = msgs
                _activeMessages.value = msgs
            }
        }
    }

    fun closeActiveConversation() {
        _activeConversation.value = null
        _activeMessages.value = emptyList()
    }

    fun sendMessage(
        agencyId: String,
        agencyName: String,
        content: String,
        replyToId: String? = null,
        replyToContent: String? = null,
        replyToSenderName: String? = null,
        onSent: () -> Unit = {}
    ) {
        val target = _activeConversation.value?.otherUserId ?: return
        val listingTitle = _activeConversation.value?.relatedListingTitle
        if (content.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(
                agencyId = agencyId,
                agencyName = agencyName,
                targetUserId = target,
                content = content,
                listingTitle = listingTitle,
                replyToId = replyToId,
                replyToContent = replyToContent,
                replyToSenderName = replyToSenderName
            ).onSuccess {
                onSent()
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newContent)
        }
    }

    fun reactToMessage(messageId: String, agencyId: String, emoji: String) {
        viewModelScope.launch {
            repository.reactToMessage(messageId, agencyId, emoji)
        }
    }
}
