package com.barberflow.scheduling.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.scheduling.domain.model.ServiceItem
import java.util.UUID

interface ServiceCatalogRepository {
    fun findById(tenantId: TenantId, serviceId: UUID): ServiceItem?
    fun findAll(tenantId: TenantId): List<ServiceItem>
    fun save(tenantId: TenantId, service: ServiceItem): ServiceItem
    fun deactivate(tenantId: TenantId, serviceId: UUID)
}
