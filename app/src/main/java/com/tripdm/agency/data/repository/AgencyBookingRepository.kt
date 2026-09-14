package com.tripdm.agency.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tripdm.agency.data.model.BookingRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AgencyBookingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeAgencyBookings(agencyId: String): Flow<List<BookingRequest>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .whereEqualTo("agencyId", agencyId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        try {
                            BookingRequest(
                                id = doc.id,
                                bookingReference = doc.getString("bookingReference") ?: doc.id.take(8).uppercase(),
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "",
                                userEmail = doc.getString("userEmail") ?: "",
                                userPhone = doc.getString("userPhone"),
                                listingId = doc.getString("listingId") ?: "",
                                listingTitle = doc.getString("listingTitle") ?: "",
                                agencyId = doc.getString("agencyId") ?: agencyId,
                                agencyName = doc.getString("agencyName") ?: "",
                                travelers = doc.getLong("travelers")?.toInt() ?: 1,
                                travelDate = doc.getString("travelDate"),
                                specialRequests = doc.getString("specialRequests"),
                                totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                                status = doc.getString("status") ?: "pending",
                                packageType = doc.getString("packageType"),
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(bookings)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("bookings").document(bookingId).update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
