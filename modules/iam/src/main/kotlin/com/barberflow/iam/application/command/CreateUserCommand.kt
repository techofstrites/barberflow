package com.barberflow.iam.application.command

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Role

data class CreateUserCommand(
    val tenantId: TenantId,
    val email: String,
    val password: String,
    val role: Role
)
