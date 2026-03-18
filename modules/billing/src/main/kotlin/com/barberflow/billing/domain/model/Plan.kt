package com.barberflow.billing.domain.model

enum class PlanTier {
    STARTER,
    GROWTH,
    ENTERPRISE
}

data class Plan(
    val tier: PlanTier,
    val name: String,
    val priceMonthly: java.math.BigDecimal,
    val maxProfessionals: Int,       // -1 = unlimited
    val maxMessagesPerMonth: Int,    // -1 = unlimited
    val features: Set<PlanFeature>
) {
    companion object {
        val STARTER = Plan(
            tier = PlanTier.STARTER,
            name = "Starter",
            priceMonthly = java.math.BigDecimal("97.00"),
            maxProfessionals = 1,
            maxMessagesPerMonth = 500,
            features = setOf(PlanFeature.BASIC_SCHEDULING, PlanFeature.WHATSAPP_BOT)
        )
        val GROWTH = Plan(
            tier = PlanTier.GROWTH,
            name = "Growth",
            priceMonthly = java.math.BigDecimal("197.00"),
            maxProfessionals = 5,
            maxMessagesPerMonth = 2000,
            features = setOf(
                PlanFeature.BASIC_SCHEDULING, PlanFeature.WHATSAPP_BOT,
                PlanFeature.AI_PREDICTIVE, PlanFeature.ADVANCED_REPORTS
            )
        )
        val ENTERPRISE = Plan(
            tier = PlanTier.ENTERPRISE,
            name = "Enterprise",
            priceMonthly = java.math.BigDecimal("397.00"),
            maxProfessionals = -1,
            maxMessagesPerMonth = -1,
            features = PlanFeature.entries.toSet()
        )

        fun fromTier(tier: PlanTier): Plan = when (tier) {
            PlanTier.STARTER -> STARTER
            PlanTier.GROWTH -> GROWTH
            PlanTier.ENTERPRISE -> ENTERPRISE
        }
    }
}
