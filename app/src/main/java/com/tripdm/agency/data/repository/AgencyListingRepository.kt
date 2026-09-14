package com.tripdm.agency.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tripdm.agency.data.model.AgencyListing
import com.tripdm.agency.data.model.ItineraryDay
import com.tripdm.agency.data.model.PlaceCovered
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AgencyListingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeAgencyListings(agencyId: String): Flow<List<AgencyListing>> = callbackFlow {
        val listener = firestore.collection("listings")
            .whereEqualTo("agencyId", agencyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        try {
                            val placesRaw = doc.get("placesCovered") as? List<Map<String, Any>> ?: emptyList()
                            val places = placesRaw.map { p ->
                                PlaceCovered(
                                    id = p["id"] as? String ?: "",
                                    name = p["name"] as? String ?: "",
                                    imageUrls = p["imageUrls"] as? List<String> ?: emptyList()
                                )
                            }

                            val itineraryRaw = doc.get("itinerary") as? List<Map<String, Any>> ?: emptyList()
                            val itinerary = itineraryRaw.map { day ->
                                ItineraryDay(
                                    day = (day["day"] as? Long)?.toInt() ?: 1,
                                    placeName = day["placeName"] as? String ?: "",
                                    description = day["description"] as? String ?: "",
                                    activities = day["activities"] as? List<String> ?: emptyList(),
                                    imageUrls = day["imageUrls"] as? List<String> ?: emptyList()
                                )
                            }

                            AgencyListing(
                                id = doc.id,
                                agencyId = doc.getString("agencyId") ?: agencyId,
                                agencyName = doc.getString("agencyName") ?: "",
                                title = doc.getString("title") ?: "",
                                packageType = doc.getString("packageType") ?: "domestic",
                                countryName = doc.getString("countryName") ?: "",
                                stateName = doc.getString("stateName") ?: "",
                                pickUpLocation = doc.getString("pickUpLocation") ?: "",
                                dropLocation = doc.getString("dropLocation") ?: "",
                                placesCovered = places,
                                tourCategories = doc.get("tourCategories") as? List<String> ?: emptyList(),
                                hotelType = doc.getString("hotelType") ?: "deluxe",
                                mealPlan = doc.getString("mealPlan") ?: "breakfast",
                                itinerary = itinerary,
                                inclusions = doc.get("inclusions") as? List<String> ?: emptyList(),
                                exclusions = doc.get("exclusions") as? List<String> ?: emptyList(),
                                cost = doc.getDouble("cost") ?: doc.getDouble("price") ?: 0.0,
                                price = doc.getDouble("price") ?: doc.getDouble("cost") ?: 0.0,
                                duration = doc.getLong("duration")?.toInt() ?: itinerary.size.coerceAtLeast(1),
                                discountCategory = doc.getString("discountCategory") ?: "none",
                                isTrending = doc.getBoolean("isTrending") ?: false,
                                season = doc.getString("season") ?: "all-seasons",
                                eventType = doc.getString("eventType") ?: "",
                                experienceType = doc.get("experienceType") as? List<String> ?: emptyList(),
                                photos = doc.get("photos") as? List<String> ?: emptyList(),
                                approved = doc.getBoolean("approved") ?: false,
                                approvalStatus = doc.getString("approvalStatus") ?: if (doc.getBoolean("approved") == true) "approved" else "pending",
                                viewsCount = doc.getLong("viewsCount")?.toInt() ?: 0,
                                inquiriesCount = doc.getLong("inquiriesCount")?.toInt() ?: 0,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(listings)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun createListing(listing: AgencyListing): Result<String> {
        return try {
            val docRef = firestore.collection("listings").document()
            val data = listing.copy(
                id = docRef.id,
                approved = false,
                approvalStatus = "pending",
                createdAt = System.currentTimeMillis()
            )
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateListing(listing: AgencyListing): Result<Unit> {
        return try {
            firestore.collection("listings").document(listing.id).set(listing).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteListing(listingId: String): Result<Unit> {
        return try {
            firestore.collection("listings").document(listingId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
