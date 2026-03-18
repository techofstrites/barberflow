package com.barberflow.customer.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerJpaRepository : JpaRepository<CustomerEntity, UUID> {
    fun findByPhoneAndTenantId(phone: String, tenantId: UUID): CustomerEntity?
    fun findByIdAndTenantId(id: UUID, tenantId: UUID): CustomerEntity?
    fun existsByPhoneAndTenantId(phone: String, tenantId: UUID): Boolean
    fun findAllByTenantId(tenantId: UUID): List<CustomerEntity>
}
