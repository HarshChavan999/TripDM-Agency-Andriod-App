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
        id = "starter_100",
        name = "Starter Pack",
        credits = 100,
        price = 999.0,
        description = "Great for small agencies just starting out.",
        features = listOf("Publish up to 5 packages", "Standard support", "Chat with travelers")
    ),
    CreditPlan(
        id = "pro_300",
        name = "Pro Growth",
        credits = 300,
        price = 2499.0,
        description = "Most popular for growing travel agencies.",
        isPopular = true,
        features = listOf("Publish up to 20 packages", "Priority lead routing", "Verified agency badge", "Email & chat support")
    ),
    CreditPlan(
        id = "enterprise_1000",
        name = "Enterprise Unlimited",
        credits = 1000,
        price = 6999.0,
        description = "For established agencies handling heavy volume.",
        features = listOf("Unlimited package listings", "Featured on homepage", "Dedicated account manager", "24/7 VIP support")
    )
)
