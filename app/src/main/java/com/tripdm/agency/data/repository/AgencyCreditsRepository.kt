package com.tripdm.agency.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tripdm.agency.data.model.CreditPlan
import com.tripdm.agency.data.model.CreditTransaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AgencyCreditsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun observeTransactions(agencyId: String): Flow<List<CreditTransaction>> = callbackFlow {
        val listener = firestore.collection("users").document(agencyId)
            .collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        CreditTransaction(
                            id = doc.id,
                            agencyId = agencyId,
                            type = doc.getString("type") ?: "purchase",
                            amount = doc.getDouble("amount") ?: 0.0,
                            credits = doc.getLong("credits")?.toInt() ?: 0,
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            status = doc.getString("status") ?: "completed"
                        )
                    }.sortedByDescending { it.timestamp }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun purchasePlan(agencyId: String, plan: CreditPlan): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(agencyId)
            firestore.runTransaction { transaction ->
                transaction.update(userRef, "credits", FieldValue.increment(plan.credits.toLong()))
                val txRef = userRef.collection("transactions").document()
                val txData = hashMapOf(
                    "id" to txRef.id,
                    "agencyId" to agencyId,
                    "type" to "purchase",
                    "amount" to plan.price,
                    "credits" to plan.credits,
                    "description" to "Purchased ${plan.name} (+${plan.credits} credits)",
                    "timestamp" to System.currentTimeMillis(),
                    "status" to "completed"
                )
                transaction.set(txRef, txData)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
