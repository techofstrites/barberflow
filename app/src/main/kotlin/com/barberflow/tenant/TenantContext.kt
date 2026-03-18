package com.barberflow.tenant

import com.barberflow.core.tenant.TenantId

object TenantContext {
    private val currentTenant = ThreadLocal<TenantId?>()

    fun set(tenantId: TenantId) = currentTenant.set(tenantId)
    fun get(): TenantId? = currentTenant.get()
    fun clear() = currentTenant.remove()

    fun require(): TenantId = get() ?: throw IllegalStateException("No tenant in context")
}
