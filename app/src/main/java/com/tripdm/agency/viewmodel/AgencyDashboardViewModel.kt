package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.AnalyticsSummary
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.repository.AgencyChatRepository
import com.tripdm.agency.data.repository.AgencyListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AgencyDashboardViewModel(
    private val listingRepo: AgencyListingRepository = AgencyListingRepository(),
    private val chatRepo: AgencyChatRepository = AgencyChatRepository()
) : ViewModel() {

    private val _analytics = MutableStateFlow(AnalyticsSummary())
    val analytics: StateFlow<AnalyticsSummary> = _analytics.asStateFlow()

    private val _recentChats = MutableStateFlow<List<ChatConversation>>(emptyList())
    val recentChats: StateFlow<List<ChatConversation>> = _recentChats.asStateFlow()

    fun initialize(agencyId: String, currentCredits: Int) {
        viewModelScope.launch {
            combine(
                listingRepo.observeAgencyListings(agencyId),
                chatRepo.observeConversations(agencyId)
            ) { listings, chats ->
                _recentChats.value = chats.take(10)

                val approvedCount = listings.count { it.approved || it.approvalStatus == "approved" }
                val pendingCount = listings.count { it.approvalStatus == "pending" && !it.approved }

                AnalyticsSummary(
                    totalListings = listings.size,
                    approvedListings = approvedCount,
                    pendingListings = pendingCount,
                    totalInquiries = chats.size,
                    totalBookings = 0,
                    confirmedBookings = 0,
                    remainingCredits = currentCredits
                )
            }.collect { summary ->
                _analytics.value = summary
            }
        }
    }
}

