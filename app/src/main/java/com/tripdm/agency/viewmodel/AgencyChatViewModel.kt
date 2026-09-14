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

    fun loadConversations(agencyId: String) {
        viewModelScope.launch {
            repository.observeConversations(agencyId).collect { list ->
                _conversations.value = list
            }
        }
    }

    fun openConversation(agencyId: String, conversation: ChatConversation) {
        _activeConversation.value = conversation
        viewModelScope.launch {
            repository.observeMessages(agencyId, conversation.otherUserId).collect { msgs ->
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
        onSent: () -> Unit = {}
    ) {
        val target = _activeConversation.value?.otherUserId ?: return
        val listingTitle = _activeConversation.value?.relatedListingTitle
        if (content.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(agencyId, agencyName, target, content, listingTitle).onSuccess {
                onSent()
            }
        }
    }
}
