package com.barberflow.billing.infrastructure.persistence

import com.barberflow.billing.domain.model.*
import com.barberflow.core.tenant.TenantId
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "subscriptions", schema = "public")
class SubscriptionEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false, unique = true)
    val tenantId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_tier", nullable = false)
    val planTier: PlanTier,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: SubscriptionStatus,

    @Column(name = "current_period_end", nullable = false)
    val currentPeriodEnd: LocalDate,

    @Column(name = "messages_sent_this_month", nullable = false)
    val messagesSentThisMonth: Int = 0,

    @Column(name = "appointments_this_month", nullable = false)
    val appointmentsThisMonth: Int = 0,

    @Column(name = "external_payment_id")
    val externalPaymentId: String? = null
) {
    fun toDomain() = Subscription.reconstitute(
        id = id,
        tenantId = TenantId(tenantId),
        plan = Plan.fromTier(planTier),
        status = status,
        currentPeriodEnd = currentPeriodEnd,
        usageMetrics = UsageMetrics(messagesSentThisMonth, appointmentsThisMonth),
        externalPaymentId = externalPaymentId
    )

    companion object {
        fun fromDomain(s: Subscription) = SubscriptionEntity(
            id = s.id,
            tenantId = s.tenantId.value,
            planTier = s.plan.tier,
            status = s.status,
            currentPeriodEnd = s.currentPeriodEnd,
            messagesSentThisMonth = s.usageMetrics.messagesSentThisMonth,
            appointmentsThisMonth = s.usageMetrics.activeAppointmentsThisMonth,
            externalPaymentId = s.externalPaymentId
        )
    }
}
