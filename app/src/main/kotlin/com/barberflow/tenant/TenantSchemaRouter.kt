package com.barberflow.tenant

import org.springframework.stereotype.Component

/**
 * Routes database operations to the correct tenant schema.
 * In a schema-per-tenant setup, this would set the Hibernate schema on each request.
 * Currently uses the system (public) schema with tenant_id column discrimination.
 */
@Component
class TenantSchemaRouter {

    fun currentSchema(): String {
        val tenantId = TenantContext.get()
        return if (tenantId != null) {
            "tenant_${tenantId.value.toString().replace("-", "_")}"
        } else {
            "public"
        }
    }
}
