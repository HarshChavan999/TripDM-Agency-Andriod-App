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

    fun observeCreditPlans(): Flow<List<CreditPlan>> = callbackFlow {
        val configRef = firestore.collection("admin").document("config")
        val listener = configRef.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && snapshot.exists()) {
                val starterPrice = (snapshot.get("starterPrice") as? Number)?.toDouble() ?: 2000.0
                val premiumPrice = (snapshot.get("premiumPrice") as? Number)?.toDouble() ?: 5000.0
                val vipPrice = (snapshot.get("vipPrice") as? Number)?.toDouble() ?: 10000.0

                val dynamicPlans = listOf(
                    CreditPlan(
                        id = "starter",
                        name = "Standard Plan",
                        credits = 200,
                        price = starterPrice,
                        description = "Best for active agencies replying to holiday inquiries.",
                        features = listOf("Publish up to 10 packages", "200 credits per lead", "Priority lead routing")
                    ),
                    CreditPlan(
                        id = "premium",
                        name = "Premium Plan",
                        credits = 500,
                        price = premiumPrice,
                        description = "For frequent high-volume agency messaging needs.",
                        isPopular = true,
                        features = listOf("Publish up to 50 packages", "175 credits per lead", "Verified agency badge", "Email & chat support")
                    ),
                    CreditPlan(
                        id = "vip",
                        name = "VIP Plan",
                        credits = 1000,
                        price = vipPrice,
                        description = "Ultimate package for top agencies wanting maximum visibility.",
                        features = listOf("Unlimited package listings", "150 credits per lead", "Featured on homepage", "Dedicated account manager")
                    )
                )
                trySend(dynamicPlans)
            } else {
                trySend(com.tripdm.agency.data.model.SampleCreditPlans)
            }
        }
        awaitClose { listener.remove() }
    }

    fun observeTransactions(agencyId: String): Flow<List<CreditTransaction>> = callbackFlow {
        val userRef = firestore.collection("users").document(agencyId)

        var subcollectionTxs = emptyList<CreditTransaction>()
        var arrayTxs = emptyList<CreditTransaction>()

        fun emitMerged() {
            val combined = (subcollectionTxs + arrayTxs)
                .distinctBy { it.id }
                .sortedByDescending { it.timestamp }
            trySend(combined)
        }

        // 1. Listen to transactions subcollection
        val subcollectionListener = userRef.collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    subcollectionTxs = snapshot.documents.mapNotNull { doc ->
                        CreditTransaction(
                            id = doc.id,
                            agencyId = agencyId,
                            type = doc.getString("type") ?: "purchase",
                            amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0,
                            credits = (doc.get("credits") as? Number)?.toInt() ?: doc.getLong("credits")?.toInt() ?: 0,
                            description = doc.getString("description") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            status = doc.getString("status") ?: "completed"
                        )
                    }
                    emitMerged()
                }
            }

        // 2. Listen to users/{agencyId} document for creditHistory array (written by WebApp/backend)
        val docListener = userRef.addSnapshotListener { snapshot, error ->
            if (error == null && snapshot != null && snapshot.exists()) {
                @Suppress("UNCHECKED_CAST")
                val historyList = snapshot.get("creditHistory") as? List<Map<String, Any>>
                if (historyList != null) {
                    arrayTxs = historyList.mapNotNull { map ->
                        val id = map["id"] as? String ?: return@mapNotNull null
                        val desc = map["description"] as? String ?: map["type"] as? String ?: "Transaction"
                        val amt = (map["amountPaid"] as? Number)?.toDouble()
                            ?: (map["amount"] as? Number)?.toDouble()
                            ?: 0.0
                        val creds = (map["credits"] as? Number)?.toInt() ?: 0
                        val ts = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        val type = map["type"] as? String ?: "purchase"
                        CreditTransaction(
                            id = id,
                            agencyId = agencyId,
                            type = type,
                            amount = amt,
                            credits = creds,
                            description = desc,
                            timestamp = ts,
                            status = "completed"
                        )
                    }
                    emitMerged()
                }
            }
        }

        awaitClose {
            subcollectionListener.remove()
            docListener.remove()
        }
    }

    suspend fun purchasePlan(agencyId: String, plan: CreditPlan): Result<Unit> {
        return try {
            val userRef = firestore.collection("users").document(agencyId)
            val now = System.currentTimeMillis()
            val txId = "TX-${plan.id.uppercase()}-${now}"

            val txMap = hashMapOf<String, Any>(
                "id" to txId,
                "agencyId" to agencyId,
                "type" to "purchase",
                "amount" to plan.price,
                "credits" to plan.credits,
                "description" to "Purchased ${plan.name} (+${plan.credits} credits)",
                "timestamp" to now,
                "status" to "completed"
            )

            firestore.runTransaction { transaction ->
                transaction.update(
                    userRef,
                    mapOf(
                        "credits" to FieldValue.increment(plan.credits.toLong()),
                        "plan" to plan.name,
                        "updatedAt" to now,
                        "creditHistory" to FieldValue.arrayUnion(txMap)
                    )
                )
                val txRef = userRef.collection("transactions").document(txId)
                transaction.set(txRef, txMap)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
