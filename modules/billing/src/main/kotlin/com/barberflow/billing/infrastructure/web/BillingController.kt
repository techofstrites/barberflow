package com.barberflow.billing.infrastructure.web

import com.barberflow.billing.application.ActivateSubscriptionCommand
import com.barberflow.billing.application.ActivateSubscriptionUseCase
import com.barberflow.billing.application.CreateTrialUseCase
import com.barberflow.billing.domain.model.PlanTier
import com.barberflow.billing.domain.model.SubscriptionStatus
import com.barberflow.billing.domain.repository.SubscriptionRepository
import com.barberflow.core.tenant.TenantId
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

data class SubscriptionResponse(
    val planTier: String,
    val status: String,
    val currentPeriodEnd: LocalDate,
    val messagesSent: Int,
    val messagesLimit: Int
)

data class ActivateRequest(
    @field:NotNull val planTier: PlanTier,
    @field:NotBlank val externalPaymentId: String
)

@RestController
@RequestMapping("/api/v1/billing")
class BillingController(
    private val subscriptionRepository: SubscriptionRepository,
    private val createTrialUseCase: CreateTrialUseCase,
    private val activateUseCase: ActivateSubscriptionUseCase
) {
    @GetMapping("/subscription")
    fun getSubscription(@RequestHeader("X-Tenant-Id") tenantId: String): SubscriptionResponse {
        val subscription = subscriptionRepository.findByTenantId(TenantId.from(tenantId))
            ?: throw NoSuchElementException("No subscription found")
        return SubscriptionResponse(
            planTier = subscription.plan.tier.name,
            status = subscription.status.name,
            currentPeriodEnd = subscription.currentPeriodEnd,
            messagesSent = subscription.usageMetrics.messagesSentThisMonth,
            messagesLimit = subscription.plan.maxMessagesPerMonth
        )
    }

    @PostMapping("/trial")
    @ResponseStatus(HttpStatus.CREATED)
    fun startTrial(@RequestHeader("X-Tenant-Id") tenantId: String): SubscriptionResponse {
        val sub = createTrialUseCase.execute(TenantId.from(tenantId))
        return SubscriptionResponse(
            planTier = sub.plan.tier.name,
            status = sub.status.name,
            currentPeriodEnd = sub.currentPeriodEnd,
            messagesSent = sub.usageMetrics.messagesSentThisMonth,
            messagesLimit = sub.plan.maxMessagesPerMonth
        )
    }

    @PostMapping("/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun activate(
        @Valid @RequestBody request: ActivateRequest,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ) {
        activateUseCase.execute(
            ActivateSubscriptionCommand(
                tenantId = TenantId.from(tenantId),
                planTier = request.planTier,
                externalPaymentId = request.externalPaymentId
            )
        )
    }
}
