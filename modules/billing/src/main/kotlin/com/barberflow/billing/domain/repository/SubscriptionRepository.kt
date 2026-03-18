package com.barberflow.billing.domain.repository

import com.barberflow.billing.domain.model.Subscription
import com.barberflow.core.tenant.TenantId

interface SubscriptionRepository {
    fun save(subscription: Subscription): Subscription
    fun findByTenantId(tenantId: TenantId): Subscription?
}
