package com.barberflow.chatbot.domain.port

import com.barberflow.core.tenant.TenantId
import java.util.UUID

interface CustomerResolverPort {
    /** Finds an existing customer by phone, or creates one if not found. Returns the customer UUID. */
    fun findOrCreate(tenantId: TenantId, phone: String): UUID
}
