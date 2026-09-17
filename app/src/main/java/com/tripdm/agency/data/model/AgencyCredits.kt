package com.tripdm.agency.data.model

data class CreditPlan(
    val id: String,
    val name: String,
    val credits: Int,
    val price: Double,
    val description: String,
    val isPopular: Boolean = false,
    val features: List<String> = emptyList()
)

data class CreditTransaction(
    val id: String = "",
    val agencyId: String = "",
    val type: String = "purchase", // "purchase", "usage", "refund"
    val amount: Double = 0.0,
    val credits: Int = 0,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "completed"
)

val SampleCreditPlans = listOf(
    CreditPlan(
        id = "starter",
        name = "Standard Plan",
        credits = 200,
        price = 2000.0,
        description = "Best for active agencies replying to holiday inquiries.",
        features = listOf("Publish up to 10 packages", "200 credits per lead", "Priority lead routing")
    ),
    CreditPlan(
        id = "premium",
        name = "Premium Plan",
        credits = 500,
        price = 5000.0,
        description = "For frequent high-volume agency messaging needs.",
        isPopular = true,
        features = listOf("Publish up to 50 packages", "175 credits per lead", "Verified agency badge", "Email & chat support")
    ),
    CreditPlan(
        id = "vip",
        name = "VIP Plan",
        credits = 1000,
        price = 10000.0,
        description = "Ultimate package for top agencies wanting maximum visibility.",
        features = listOf("Unlimited package listings", "150 credits per lead", "Featured on homepage", "Dedicated account manager")
    )
)
