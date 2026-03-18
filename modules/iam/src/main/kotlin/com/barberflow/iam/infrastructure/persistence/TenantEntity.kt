package com.barberflow.iam.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.model.Tenant
import com.barberflow.iam.domain.model.TenantStatus
import com.barberflow.iam.domain.model.WhatsAppConfig
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tenants", schema = "public")
class TenantEntity(
    @Id
    val id: UUID,

    @Column(unique = true, nullable = false)
    val slug: String,

    @Column(nullable = false)
    val name: String,

    @Column(name = "whatsapp_phone_number_id")
    val whatsappPhoneNumberId: String? = null,

    @Column(name = "whatsapp_access_token", length = 1000)
    val whatsappAccessToken: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TenantStatus = TenantStatus.ONBOARDING,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    fun toDomain(): Tenant {
        val tenant = Tenant.reconstitute(
            id = TenantId(id),
            slug = slug,
            name = name,
            whatsAppConfig = if (whatsappPhoneNumberId != null && whatsappAccessToken != null)
                WhatsAppConfig(whatsappPhoneNumberId, whatsappAccessToken) else null,
            status = status,
            createdAt = createdAt
        )
        return tenant
    }

    companion object {
        fun fromDomain(tenant: Tenant) = TenantEntity(
            id = tenant.id.value,
            slug = tenant.slug,
            name = tenant.name,
            whatsappPhoneNumberId = tenant.whatsAppConfig?.phoneNumberId,
            whatsappAccessToken = tenant.whatsAppConfig?.accessToken,
            status = tenant.status,
            createdAt = tenant.createdAt
        )
    }
}
