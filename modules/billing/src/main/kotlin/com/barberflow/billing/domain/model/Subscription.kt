package com.barberflow.billing.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.billing.domain.event.SubscriptionActivated
import com.barberflow.billing.domain.event.SubscriptionCancelled
import com.barberflow.billing.domain.event.SubscriptionOverLimit
import java.time.LocalDate
import java.util.UUID

class Subscription private constructor(
    val id: UUID,
    val tenantId: TenantId,
    var plan: Plan,
    var status: SubscriptionStatus,
    var currentPeriodEnd: LocalDate,
    var usageMetrics: UsageMetrics,
    var externalPaymentId: String?
) : AggregateRoot() {

    companion object {
        val TRIAL_DAYS = 14L

        fun startTrial(tenantId: TenantId): Subscription {
            return Subscription(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                plan = Plan.STARTER,
                status = SubscriptionStatus.TRIALING,
                currentPeriodEnd = LocalDate.now().plusDays(TRIAL_DAYS),
                usageMetrics = UsageMetrics(),
                externalPaymentId = null
            )
        }

        fun reconstitute(
            id: UUID, tenantId: TenantId, plan: Plan, status: SubscriptionStatus,
            currentPeriodEnd: LocalDate, usageMetrics: UsageMetrics, externalPaymentId: String?
        ) = Subscription(id, tenantId, plan, status, currentPeriodEnd, usageMetrics, externalPaymentId)
    }

    fun activate(planTier: PlanTier, periodEnd: LocalDate, paymentId: String) {
        plan = Plan.fromTier(planTier)
        status = SubscriptionStatus.ACTIVE
        currentPeriodEnd = periodEnd
        externalPaymentId = paymentId
        registerEvent(SubscriptionActivated(id, tenantId, plan.tier, plan.features))
    }

    fun cancel() {
        status = SubscriptionStatus.CANCELLED
        registerEvent(SubscriptionCancelled(id, tenantId))
    }

    fun markPastDue() {
        status = SubscriptionStatus.PAST_DUE
    }

    fun recordMessageSent() {
        val limit = plan.maxMessagesPerMonth
        if (limit != -1 && usageMetrics.messagesSentThisMonth >= limit) {
            registerEvent(SubscriptionOverLimit(id, tenantId, "messages", usageMetrics.messagesSentThisMonth))
            return
        }
        usageMetrics = usageMetrics.incrementMessages()
    }

    fun resetMonthlyUsage() {
        usageMetrics = usageMetrics.reset()
    }

    fun hasFeature(feature: PlanFeature): Boolean = plan.features.contains(feature)

    fun isActive(): Boolean = status in listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)
}
