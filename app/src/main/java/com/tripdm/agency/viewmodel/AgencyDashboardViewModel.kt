package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.AnalyticsSummary
import com.tripdm.agency.data.model.BookingRequest
import com.tripdm.agency.data.model.ChatConversation
import com.tripdm.agency.data.repository.AgencyBookingRepository
import com.tripdm.agency.data.repository.AgencyChatRepository
import com.tripdm.agency.data.repository.AgencyListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class AgencyDashboardViewModel(
    private val listingRepo: AgencyListingRepository = AgencyListingRepository(),
    private val bookingRepo: AgencyBookingRepository = AgencyBookingRepository(),
    private val chatRepo: AgencyChatRepository = AgencyChatRepository()
) : ViewModel() {

    private val _analytics = MutableStateFlow(AnalyticsSummary())
    val analytics: StateFlow<AnalyticsSummary> = _analytics.asStateFlow()

    private val _recentBookings = MutableStateFlow<List<BookingRequest>>(emptyList())
    val recentBookings: StateFlow<List<BookingRequest>> = _recentBookings.asStateFlow()

    private val _recentChats = MutableStateFlow<List<ChatConversation>>(emptyList())
    val recentChats: StateFlow<List<ChatConversation>> = _recentChats.asStateFlow()

    fun initialize(agencyId: String, currentCredits: Int) {
        viewModelScope.launch {
            combine(
                listingRepo.observeAgencyListings(agencyId),
                bookingRepo.observeAgencyBookings(agencyId),
                chatRepo.observeConversations(agencyId)
            ) { listings, bookings, chats ->
                _recentBookings.value = bookings.take(5)
                _recentChats.value = chats.take(5)

                val approvedCount = listings.count { it.approved || it.approvalStatus == "approved" }
                val pendingCount = listings.count { it.approvalStatus == "pending" && !it.approved }
                val confirmedBookingsCount = bookings.count { it.status.lowercase() == "confirmed" }

                AnalyticsSummary(
                    totalListings = listings.size,
                    approvedListings = approvedCount,
                    pendingListings = pendingCount,
                    totalInquiries = chats.size,
                    totalBookings = bookings.size,
                    confirmedBookings = confirmedBookingsCount,
                    remainingCredits = currentCredits
                )
            }.collect { summary ->
                _analytics.value = summary
            }
        }
    }
}
