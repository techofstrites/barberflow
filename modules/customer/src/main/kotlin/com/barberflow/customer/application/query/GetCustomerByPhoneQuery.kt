package com.barberflow.customer.application.query

import com.barberflow.core.tenant.TenantId
import com.barberflow.customer.domain.model.Customer
import com.barberflow.customer.domain.repository.CustomerRepository
import org.springframework.stereotype.Service

data class GetCustomerByPhoneQuery(
    val phone: String,
    val tenantId: TenantId
)

@Service
class GetCustomerByPhoneQueryHandler(
    private val customerRepository: CustomerRepository
) {
    fun handle(query: GetCustomerByPhoneQuery): Customer? =
        customerRepository.findByPhone(query.phone, query.tenantId)
}
