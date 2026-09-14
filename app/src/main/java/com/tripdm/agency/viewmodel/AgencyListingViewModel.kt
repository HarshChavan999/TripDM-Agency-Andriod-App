package com.tripdm.agency.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.repository.AgencyListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ListingFilterTab {
    ALL,
    APPROVED,
    PENDING,
    REJECTED
}

class AgencyListingViewModel(
    private val repository: AgencyListingRepository = AgencyListingRepository()
) : ViewModel() {

    private val _rawListings = MutableStateFlow<List<AgencyListing>>(emptyList())
    private val _selectedTab = MutableStateFlow(ListingFilterTab.ALL)
    val selectedTab: StateFlow<ListingFilterTab> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredListings = MutableStateFlow<List<AgencyListing>>(emptyList())
    val filteredListings: StateFlow<List<AgencyListing>> = _filteredListings.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    fun loadAgencyListings(agencyId: String) {
        viewModelScope.launch {
            repository.observeAgencyListings(agencyId).collect { list ->
                _rawListings.value = list
                applyFilters()
            }
        }
    }

    fun setFilterTab(tab: ListingFilterTab) {
        _selectedTab.value = tab
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    private fun applyFilters() {
        val tab = _selectedTab.value
        val query = _searchQuery.value.trim().lowercase()

        val tabFiltered = when (tab) {
            ListingFilterTab.ALL -> _rawListings.value
            ListingFilterTab.APPROVED -> _rawListings.value.filter { it.approved || it.approvalStatus == "approved" }
            ListingFilterTab.PENDING -> _rawListings.value.filter { it.approvalStatus == "pending" && !it.approved }
            ListingFilterTab.REJECTED -> _rawListings.value.filter { it.approvalStatus == "rejected" }
        }

        _filteredListings.value = if (query.isEmpty()) {
            tabFiltered
        } else {
            tabFiltered.filter {
                it.title.lowercase().contains(query) ||
                it.stateName.lowercase().contains(query) ||
                it.countryName.lowercase().contains(query)
            }
        }
    }

    fun createListing(listing: AgencyListing, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = repository.createListing(listing)
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _operationMessage.value = "Package submitted for approval successfully!"
                    onComplete(true)
                },
                onFailure = { err ->
                    _operationMessage.value = "Error: ${err.message}"
                    onComplete(false)
                }
            )
        }
    }

    fun updateListing(listing: AgencyListing, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = repository.updateListing(listing)
            _isSubmitting.value = false
            res.fold(
                onSuccess = {
                    _operationMessage.value = "Package updated successfully!"
                    onComplete(true)
                },
                onFailure = { err ->
                    _operationMessage.value = "Error: ${err.message}"
                    onComplete(false)
                }
            )
        }
    }

    fun deleteListing(listingId: String) {
        viewModelScope.launch {
            repository.deleteListing(listingId).fold(
                onSuccess = { _operationMessage.value = "Package deleted." },
                onFailure = { err -> _operationMessage.value = "Failed to delete: ${err.message}" }
            )
        }
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }
}
