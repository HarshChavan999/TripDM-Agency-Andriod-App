package com.tripdm.agency.data.model

data class AnalyticsSummary(
    val totalListings: Int = 0,
    val approvedListings: Int = 0,
    val pendingListings: Int = 0,
    val totalInquiries: Int = 0,
    val totalBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val remainingCredits: Int = 0
)
