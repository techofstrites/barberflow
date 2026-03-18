package com.barberflow.iam.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.User
import java.util.UUID

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findAllByTenantId(tenantId: TenantId): List<User>
}
