package com.barberflow.billing.infrastructure.persistence

import com.barberflow.billing.domain.model.Subscription
import com.barberflow.billing.domain.repository.SubscriptionRepository
import com.barberflow.core.tenant.TenantId
import org.springframework.stereotype.Repository

@Repository
class SubscriptionRepositoryImpl(
    private val jpa: SubscriptionJpaRepository
) : SubscriptionRepository {

    override fun save(subscription: Subscription): Subscription {
        jpa.save(SubscriptionEntity.fromDomain(subscription))
        return subscription
    }

    override fun findByTenantId(tenantId: TenantId): Subscription? =
        jpa.findByTenantId(tenantId.value)?.toDomain()
}
