package com.barberflow.billing.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SubscriptionJpaRepository : JpaRepository<SubscriptionEntity, UUID> {
    fun findByTenantId(tenantId: UUID): SubscriptionEntity?
}
