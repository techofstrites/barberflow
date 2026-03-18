package com.barberflow.iam.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Role
import com.barberflow.iam.domain.model.User
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users", schema = "public")
class UserEntity(
    @Id
    val id: UUID,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toDomain() = User.reconstitute(
        id = id,
        tenantId = TenantId(tenantId),
        email = email,
        passwordHash = passwordHash,
        role = role,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User) = UserEntity(
            id = user.id,
            tenantId = user.tenantId.value,
            email = user.email,
            passwordHash = user.passwordHash,
            role = user.role,
            createdAt = user.createdAt
        )
    }
}
