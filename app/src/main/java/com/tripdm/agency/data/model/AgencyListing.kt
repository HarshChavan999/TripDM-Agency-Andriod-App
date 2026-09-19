package com.tripdm.agency.data.model

data class AgencyListing(
    val id: String = "",
    val agencyId: String = "",
    val agencyName: String = "",
    val title: String = "",
    val packageType: String = "domestic", // "domestic" or "international"
    val countryName: String = "",
    val stateName: String = "",
    // Multi-value lists (preferred, matches WebApp)
    val countryNames: List<String> = emptyList(),
    val stateNames: List<String> = emptyList(),
    val pickUpLocation: String = "",
    val dropLocation: String = "",
    val placesCovered: List<PlaceCovered> = emptyList(),
    val tourCategories: List<String> = emptyList(), // "Family", "Honeymoon", "Friends", "Religious", "Fix Departure"
    // Multi-select hotel & meal (matches WebApp)
    val hotelTypes: List<String> = emptyList(), // "budget", "deluxe", "premium"
    val mealPlans: List<String> = emptyList(),   // "no-meal", "breakfast", "lunch", "dinner", "all-meals", etc.
    // Legacy single-value fields kept for backward compatibility
    val hotelType: String = "deluxe",
    val mealPlan: String = "breakfast",
    val itinerary: List<ItineraryDay> = emptyList(),
    val inclusions: List<String> = emptyList(),
    val exclusions: List<String> = emptyList(),
    val cost: Double = 0.0,
    val price: Double = 0.0,
    val duration: Int = 1, // days (auto-calculated from itinerary size)
    val discountCategory: String = "none", // "none", "10-off", "50-off", "flash-deals"
    val isTrending: Boolean = false,
    val season: String = "", // "summer", "monsoon", "winter", "spring", "all-seasons"
    val eventType: String = "", // "new-year", "diwali", "summer-vacation", "weekend"
    val experienceType: List<String> = emptyList(),
    val photos: List<String> = emptyList(),
    val approved: Boolean = false,
    val approvalStatus: String = "pending", // "pending", "approved", "rejected"
    val viewsCount: Int = 0,
    val inquiriesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlaceCovered(
    val id: String = "",
    val name: String = "",
    val imageUrls: List<String> = emptyList()
)

data class ItineraryDay(
    val day: Int = 1,
    val placeName: String = "",
    val description: String = "",
    val activities: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList()
)
