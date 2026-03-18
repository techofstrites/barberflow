package com.barberflow.billing.application

import com.barberflow.billing.domain.model.Subscription
import com.barberflow.billing.domain.repository.SubscriptionRepository
import com.barberflow.core.tenant.TenantId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateTrialUseCase(
    private val subscriptionRepository: SubscriptionRepository
) {
    @Transactional
    fun execute(tenantId: TenantId): Subscription {
        val existing = subscriptionRepository.findByTenantId(tenantId)
        if (existing != null) return existing

        val trial = Subscription.startTrial(tenantId)
        return subscriptionRepository.save(trial)
    }
}
