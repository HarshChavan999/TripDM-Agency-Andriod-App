package com.tripdm.agency.data.model

data class AgencyProfile(
    val id: String = "",
    val companyName: String = "",
    val contactPersonName: String = "",
    val email: String = "",
    val phone: String = "",
    val countryCode: String = "+91",
    val businessLocation: String = "",
    val fullAddress: String = "",
    val description: String = "",
    val operatingFromHome: Boolean = false,
    val operatingFromOffice: Boolean = false,
    val officeAddress: String = "",
    val refundPolicy: String = "",
    val approved: Boolean = false,
    val approvalStatus: String = "pending", // "pending", "approved", "rejected"
    val logoUrl: String = "",
    val credits: Int = 100,
    val plan: String = "Free",
    val role: String = "agency",
    val createdAt: Long = System.currentTimeMillis()
)
