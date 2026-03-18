package com.barberflow.core.tenant

import java.util.UUID

data class TenantId(val value: UUID) {
    companion object {
        fun new() = TenantId(UUID.randomUUID())
        fun from(value: String) = TenantId(UUID.fromString(value))
    }
    override fun toString() = value.toString()
}
