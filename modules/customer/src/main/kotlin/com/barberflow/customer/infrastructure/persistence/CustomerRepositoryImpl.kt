package com.barberflow.customer.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.model.Customer
import com.barberflow.customer.domain.repository.CustomerRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class CustomerRepositoryImpl(
    private val jpa: CustomerJpaRepository
) : CustomerRepository {

    override fun save(customer: Customer): Customer {
        jpa.save(CustomerEntity.fromDomain(customer))
        return customer
    }

    override fun findById(id: UUID, tenantId: TenantId): Customer? =
        jpa.findByIdAndTenantId(id, tenantId.value)?.toDomain()

    override fun findByPhone(phone: String, tenantId: TenantId): Customer? =
        jpa.findByPhoneAndTenantId(phone, tenantId.value)?.toDomain()

    override fun existsByPhone(phone: String, tenantId: TenantId): Boolean =
        jpa.existsByPhoneAndTenantId(phone, tenantId.value)

    override fun findAll(tenantId: TenantId): List<Customer> =
        jpa.findAllByTenantId(tenantId.value).map { it.toDomain() }

    override fun delete(id: UUID, tenantId: TenantId) =
        jpa.deleteByIdAndTenantId(id, tenantId.value)
}
