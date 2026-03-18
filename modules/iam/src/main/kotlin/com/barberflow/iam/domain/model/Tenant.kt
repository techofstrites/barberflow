package com.barberflow.iam.domain.model

import com.barberflow.core.domain.AggregateRoot
import com.barberflow.core.tenant.TenantId
import com.barberflow.iam.domain.event.TenantCreated
import java.time.Instant
import java.util.UUID

enum class TenantStatus { ONBOARDING, ACTIVE, SUSPENDED }

class Tenant private constructor(
    val id: TenantId,
    val slug: String,
    val name: String,
    var whatsAppConfig: WhatsAppConfig?,
    var status: TenantStatus,
    val createdAt: Instant
) : AggregateRoot() {

    companion object {
        fun create(slug: String, name: String): Tenant {
            val tenant = Tenant(
                id = TenantId.new(),
                slug = slug,
                name = name,
                whatsAppConfig = null,
                status = TenantStatus.ONBOARDING,
                createdAt = Instant.now()
            )
            tenant.registerEvent(TenantCreated(tenant.id, slug, name))
            return tenant
        }

        fun reconstitute(
            id: TenantId,
            slug: String,
            name: String,
            whatsAppConfig: WhatsAppConfig?,
            status: TenantStatus,
            createdAt: Instant
        ) = Tenant(id, slug, name, whatsAppConfig, status, createdAt)
    }

    fun configureWhatsApp(config: WhatsAppConfig) {
        whatsAppConfig = config
        if (status == TenantStatus.ONBOARDING) status = TenantStatus.ACTIVE
    }

    fun suspend() {
        status = TenantStatus.SUSPENDED
    }
}
