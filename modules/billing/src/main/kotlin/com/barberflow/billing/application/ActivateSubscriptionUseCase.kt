package com.barberflow.billing.application

import com.barberflow.billing.domain.model.PlanTier
import com.barberflow.billing.domain.repository.SubscriptionRepository
import com.barberflow.core.tenant.TenantId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

data class ActivateSubscriptionCommand(
    val tenantId: TenantId,
    val planTier: PlanTier,
    val externalPaymentId: String,
    val periodEnd: LocalDate = LocalDate.now().plusMonths(1)
)

@Service
class ActivateSubscriptionUseCase(
    private val subscriptionRepository: SubscriptionRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(command: ActivateSubscriptionCommand) {
        val subscription = subscriptionRepository.findByTenantId(command.tenantId)
            ?: throw NoSuchElementException("Subscription not found for tenant ${command.tenantId}")

        subscription.activate(command.planTier, command.periodEnd, command.externalPaymentId)
        subscriptionRepository.save(subscription)
        subscription.domainEvents.forEach { eventPublisher.publishEvent(it) }
        subscription.clearEvents()
    }
}
