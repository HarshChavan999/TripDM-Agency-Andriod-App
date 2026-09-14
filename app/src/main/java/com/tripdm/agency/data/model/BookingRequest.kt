package com.tripdm.agency.data.model

data class BookingRequest(
    val id: String = "",
    val bookingReference: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String? = null,
    val listingId: String = "",
    val listingTitle: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val travelers: Int = 1,
    val travelDate: String? = null,
    val specialRequests: String? = null,
    val totalAmount: Double = 0.0,
    val status: String = "pending", // "pending", "confirmed", "cancelled"
    val packageType: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
