package com.barberflow.iam.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Tenant

interface TenantRepository {
    fun save(tenant: Tenant): Tenant
    fun findById(id: TenantId): Tenant?
    fun findBySlug(slug: String): Tenant?
    fun existsBySlug(slug: String): Boolean
}
