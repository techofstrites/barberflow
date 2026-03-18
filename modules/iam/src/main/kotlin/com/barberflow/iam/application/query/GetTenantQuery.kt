package com.barberflow.iam.application.query

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Tenant
import com.barberflow.iam.domain.repository.TenantRepository
import org.springframework.stereotype.Service

data class GetTenantQuery(val tenantId: TenantId)

@Service
class GetTenantQueryHandler(
    private val tenantRepository: TenantRepository
) {
    fun handle(query: GetTenantQuery): Tenant? =
        tenantRepository.findById(query.tenantId)
}
