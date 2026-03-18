package com.barberflow.iam.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.event.UserCreated
import java.time.Instant
import java.util.UUID

class User private constructor(
    val id: UUID,
    val tenantId: TenantId,
    val email: String,
    var passwordHash: String,
    var role: Role,
    val createdAt: Instant
) : AggregateRoot() {

    companion object {
        fun create(tenantId: TenantId, email: String, passwordHash: String, role: Role): User {
            val user = User(
                id = UUID.randomUUID(),
                tenantId = tenantId,
                email = email,
                passwordHash = passwordHash,
                role = role,
                createdAt = Instant.now()
            )
            user.registerEvent(UserCreated(user.id, tenantId, email, role))
            return user
        }

        fun reconstitute(
            id: UUID,
            tenantId: TenantId,
            email: String,
            passwordHash: String,
            role: Role,
            createdAt: Instant
        ) = User(id, tenantId, email, passwordHash, role, createdAt)
    }
}
