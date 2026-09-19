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
        val trimmedId = agencyId.trim()
        val listener = firestore.collection("listings")
            .whereEqualTo("agencyId", trimmedId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("AgencyListingRepo", "Error observing listings: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val listings = snapshot.documents.mapNotNull { doc ->
                        try {
                            parseListingDocument(doc, trimmedId)
                        } catch (e: Exception) {
                            android.util.Log.e("AgencyListingRepo", "Error parsing listing ${doc.id}: ${e.message}", e)
                            null
                        }
                    }
                    android.util.Log.d("AgencyListingRepo", "Parsed ${listings.size} listings for agency $trimmedId")
                    trySend(listings)
                }
            }
        awaitClose { listener.remove() }
    }

    private fun parseListingDocument(doc: com.google.firebase.firestore.DocumentSnapshot, fallbackAgencyId: String): AgencyListing {
        val placesRaw = doc.get("placesCovered")
        val places: List<PlaceCovered> = when (placesRaw) {
            is List<*> -> placesRaw.mapIndexedNotNull { index, item ->
                when (item) {
                    is Map<*, *> -> PlaceCovered(
                        id = item["id"]?.toString() ?: "place_$index",
                        name = item["name"]?.toString() ?: item["placeName"]?.toString() ?: "",
                        imageUrls = item["imageUrls"].toStringList().ifEmpty { item["photos"].toStringList() }
                    )
                    is String -> PlaceCovered(
                        id = "place_$index",
                        name = item,
                        imageUrls = emptyList()
                    )
                    else -> null
                }
            }
            else -> emptyList()
        }

        val itineraryRaw = doc.get("itinerary")
        val itinerary: List<ItineraryDay> = when (itineraryRaw) {
            is List<*> -> itineraryRaw.mapIndexedNotNull { index, item ->
                when (item) {
                    is Map<*, *> -> ItineraryDay(
                        day = item["day"].toIntSafe(index + 1),
                        placeName = item["placeName"]?.toString()
                            ?: item["title"]?.toString()
                            ?: item["name"]?.toString()
                            ?: "Day ${index + 1}",
                        description = item["description"]?.toString() ?: "",
                        activities = item["activities"].toStringList(),
                        imageUrls = item["imageUrls"].toStringList().ifEmpty { item["photos"].toStringList() }
                    )
                    is String -> ItineraryDay(
                        day = index + 1,
                        placeName = "Day ${index + 1}",
                        description = item,
                        activities = emptyList(),
                        imageUrls = emptyList()
                    )
                    else -> null
                }
            }
            else -> emptyList()
        }

        val costVal = doc.get("cost").toDoubleSafe()
        val priceVal = doc.get("price").toDoubleSafe()
        val finalCost = if (costVal > 0.0) costVal else priceVal
        val finalPrice = if (priceVal > 0.0) priceVal else costVal

        val stateNameVal = doc.get("stateName").toSingleString().ifEmpty {
            doc.get("stateNames").toStringList().joinToString(", ")
        }

        val countryNameVal = doc.get("countryName").toSingleString().ifEmpty {
            doc.get("countryNames").toStringList().firstOrNull() ?: ""
        }
        val countryNamesVal = doc.get("countryNames").toStringList().ifEmpty {
            listOfNotNull(countryNameVal.takeIf { it.isNotBlank() })
        }

        val stateNamesVal = doc.get("stateNames").toStringList().ifEmpty {
            listOfNotNull(stateNameVal.takeIf { it.isNotBlank() })
        }

        val hotelTypeVal = doc.get("hotelType").toSingleString().ifEmpty {
            doc.get("hotelTypes").toStringList().firstOrNull() ?: "deluxe"
        }
        val hotelTypesVal = doc.get("hotelTypes").toStringList().ifEmpty {
            listOfNotNull(hotelTypeVal.takeIf { it.isNotBlank() })
        }

        val mealPlanVal = doc.get("mealPlan").toSingleString().ifEmpty {
            doc.get("mealPlans").toStringList().firstOrNull() ?: "breakfast"
        }
        val mealPlansVal = doc.get("mealPlans").toStringList().ifEmpty {
            listOfNotNull(mealPlanVal.takeIf { it.isNotBlank() })
        }

        val photoList = (
            doc.get("photos").toStringList() +
            doc.get("imageUrls").toStringList() +
            doc.get("images").toStringList() +
            listOfNotNull(doc.get("imageUrl").toSingleString().takeIf { it.isNotBlank() })
        ).distinct()

        val isApproved = doc.get("approved").toBooleanSafe() ||
                doc.get("approvalStatus").toSingleString().equals("approved", ignoreCase = true)

        val approvalStatus = doc.get("approvalStatus").toSingleString().ifEmpty {
            if (isApproved) "approved" else "pending"
        }

        val durationVal = doc.get("duration").toIntSafe(0).let {
            if (it > 0) it else itinerary.size.coerceAtLeast(1)
        }

        val resolvedAgencyId = doc.get("agencyId").toSingleString().ifEmpty {
            doc.get("agency_id").toSingleString(fallbackAgencyId)
        }

        return AgencyListing(
            id = doc.id,
            agencyId = resolvedAgencyId,
            agencyName = doc.get("agencyName").toSingleString().ifEmpty { doc.get("companyName").toSingleString() },
            title = doc.get("title").toSingleString().ifEmpty { doc.get("name").toSingleString() },
            packageType = doc.get("packageType").toSingleString("domestic"),
            countryName = countryNameVal,
            stateName = stateNameVal,
            countryNames = countryNamesVal,
            stateNames = stateNamesVal,
            pickUpLocation = doc.get("pickUpLocation").toSingleString(),
            dropLocation = doc.get("dropLocation").toSingleString(),
            placesCovered = places,
            tourCategories = doc.get("tourCategories").toStringList(),
            hotelType = hotelTypeVal,
            hotelTypes = hotelTypesVal,
            mealPlan = mealPlanVal,
            mealPlans = mealPlansVal,
            itinerary = itinerary,
            inclusions = doc.get("inclusions").toStringList().ifEmpty { doc.get("defaultInclusions").toStringList() },
            exclusions = doc.get("exclusions").toStringList().ifEmpty { doc.get("defaultExclusions").toStringList() },
            cost = finalCost,
            price = finalPrice,
            duration = durationVal,
            discountCategory = doc.get("discountCategory").toSingleString("none"),
            isTrending = doc.get("isTrending").toBooleanSafe(),
            season = doc.get("season").toSingleString(),
            eventType = doc.get("eventType").toSingleString(),
            experienceType = doc.get("experienceType").toStringList(),
            photos = photoList,
            approved = isApproved,
            approvalStatus = approvalStatus,
            viewsCount = doc.get("viewsCount").toIntSafe(0),
            inquiriesCount = doc.get("inquiriesCount").toIntSafe(0),
            createdAt = doc.get("createdAt").toLongSafe(System.currentTimeMillis())
        )
    }

    private fun Any?.toSingleString(default: String = ""): String {
        return when (this) {
            is String -> this.trim()
            is List<*> -> this.firstOrNull()?.toString()?.trim() ?: default
            null -> default
            else -> this.toString().trim()
        }
    }

    private fun Any?.toDoubleSafe(default: Double = 0.0): Double {
        return when (this) {
            is Number -> this.toDouble()
            is String -> this.trim().toDoubleOrNull() ?: default
            else -> default
        }
    }

    private fun Any?.toIntSafe(default: Int = 0): Int {
        return when (this) {
            is Number -> this.toInt()
            is String -> this.trim().toIntOrNull() ?: default
            else -> default
        }
    }

    private fun Any?.toLongSafe(default: Long = System.currentTimeMillis()): Long {
        return when (this) {
            is com.google.firebase.Timestamp -> this.toDate().time
            is Number -> this.toLong()
            is String -> this.trim().toLongOrNull() ?: default
            is java.util.Date -> this.time
            else -> default
        }
    }

    private fun Any?.toBooleanSafe(default: Boolean = false): Boolean {
        return when (this) {
            is Boolean -> this
            is String -> this.equals("true", ignoreCase = true) || this.equals("approved", ignoreCase = true)
            is Number -> this.toInt() != 0
            else -> default
        }
    }

    private fun Any?.toStringList(): List<String> {
        return when (this) {
            is List<*> -> this.mapNotNull { item ->
                when (item) {
                    is String -> item.trim().takeIf { it.isNotEmpty() }
                    null -> null
                    else -> item.toString().trim().takeIf { it.isNotEmpty() }
                }
            }
            is String -> {
                val s = this.trim()
                if (s.isEmpty()) emptyList()
                else if (s.contains("\n")) s.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                else if (s.contains(",")) s.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                else listOf(s)
            }
            else -> emptyList()
        }
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
