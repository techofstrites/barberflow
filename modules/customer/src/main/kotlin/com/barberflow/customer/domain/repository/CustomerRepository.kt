package com.barberflow.customer.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.model.Customer
import java.util.UUID

interface CustomerRepository {
    fun save(customer: Customer): Customer
    fun findById(id: UUID, tenantId: TenantId): Customer?
    fun findByPhone(phone: String, tenantId: TenantId): Customer?
    fun existsByPhone(phone: String, tenantId: TenantId): Boolean
    fun findAll(tenantId: TenantId): List<Customer>
    fun delete(id: UUID, tenantId: TenantId)
}
