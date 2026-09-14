package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.BookingRequest
import com.tripdm.agency.data.repository.AgencyBookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BookingFilterTab {
    ALL,
    PENDING,
    CONFIRMED,
    CANCELLED
}

class AgencyBookingViewModel(
    private val repository: AgencyBookingRepository = AgencyBookingRepository()
) : ViewModel() {

    private val _rawBookings = MutableStateFlow<List<BookingRequest>>(emptyList())
    private val _selectedTab = MutableStateFlow(BookingFilterTab.ALL)
    val selectedTab: StateFlow<BookingFilterTab> = _selectedTab.asStateFlow()

    private val _filteredBookings = MutableStateFlow<List<BookingRequest>>(emptyList())
    val filteredBookings: StateFlow<List<BookingRequest>> = _filteredBookings.asStateFlow()

    fun loadAgencyBookings(agencyId: String) {
        viewModelScope.launch {
            repository.observeAgencyBookings(agencyId).collect { list ->
                _rawBookings.value = list
                applyFilters()
            }
        }
    }

    fun setFilterTab(tab: BookingFilterTab) {
        _selectedTab.value = tab
        applyFilters()
    }

    private fun applyFilters() {
        val tab = _selectedTab.value
        _filteredBookings.value = when (tab) {
            BookingFilterTab.ALL -> _rawBookings.value
            BookingFilterTab.PENDING -> _rawBookings.value.filter { it.status.lowercase() == "pending" }
            BookingFilterTab.CONFIRMED -> _rawBookings.value.filter { it.status.lowercase() == "confirmed" }
            BookingFilterTab.CANCELLED -> _rawBookings.value.filter { it.status.lowercase() == "cancelled" }
        }
    }

    fun updateStatus(bookingId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, newStatus)
        }
    }
}
