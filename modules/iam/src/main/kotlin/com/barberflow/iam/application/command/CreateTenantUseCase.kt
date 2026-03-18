package com.barberflow.iam.application.command

import com.barberflow.iam.domain.model.Role
import com.barberflow.iam.domain.model.Tenant
import com.barberflow.iam.domain.model.User
import com.barberflow.iam.domain.repository.TenantRepository
import com.barberflow.iam.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class CreateTenantResult(val tenantId: String, val adminUserId: String)

@Service
class CreateTenantUseCase(
    private val tenantRepository: TenantRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(command: CreateTenantCommand): CreateTenantResult {
        require(!tenantRepository.existsBySlug(command.slug)) {
            "Slug '${command.slug}' already in use"
        }

        val tenant = Tenant.create(command.slug, command.name)
        tenantRepository.save(tenant)

        val admin = User.create(
            tenantId = tenant.id,
            email = command.adminEmail,
            passwordHash = passwordEncoder.encode(command.adminPassword),
            role = Role.TENANT_ADMIN
        )
        userRepository.save(admin)

        return CreateTenantResult(tenant.id.toString(), admin.id.toString())
    }
}
