package com.barberflow.customer.application.command

import com.barberflow.core.tenant.TenantId

data class CreateCustomerCommand(
    val tenantId: TenantId,
    val phone: String,
    val name: String,
    val consentGiven: Boolean = false
)
