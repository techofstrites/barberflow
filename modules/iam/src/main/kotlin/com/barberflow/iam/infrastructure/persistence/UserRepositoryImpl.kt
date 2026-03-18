package com.barberflow.iam.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.User
import com.barberflow.iam.domain.repository.UserRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val jpa: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User {
        jpa.save(UserEntity.fromDomain(user))
        return user
    }

    override fun findById(id: UUID): User? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun findByEmail(email: String): User? =
        jpa.findByEmail(email)?.toDomain()

    override fun findAllByTenantId(tenantId: TenantId): List<User> =
        jpa.findAllByTenantId(tenantId.value).map { it.toDomain() }
}
