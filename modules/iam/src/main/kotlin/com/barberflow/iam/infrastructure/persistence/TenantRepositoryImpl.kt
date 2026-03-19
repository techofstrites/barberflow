package com.barberflow.iam.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Tenant
import com.barberflow.iam.domain.repository.TenantRepository
import org.springframework.stereotype.Repository

@Repository
class TenantRepositoryImpl(
    private val jpa: TenantJpaRepository
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant {
        jpa.save(TenantEntity.fromDomain(tenant))
        return tenant
    }

    override fun findById(id: TenantId): Tenant? =
        jpa.findById(id.value).orElse(null)?.toDomain()

    override fun findBySlug(slug: String): Tenant? =
        jpa.findBySlug(slug)?.toDomain()

    override fun findByWhatsAppPhoneNumberId(phoneNumberId: String): Tenant? =
        jpa.findByWhatsappPhoneNumberId(phoneNumberId)?.toDomain()

    override fun existsBySlug(slug: String): Boolean =
        jpa.existsBySlug(slug)
}
