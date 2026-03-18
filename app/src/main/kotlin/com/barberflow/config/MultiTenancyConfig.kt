package com.barberflow.config

import org.springframework.context.annotation.Configuration

/**
 * Multi-tenancy configuration.
 * Uses tenant_id column discrimination strategy (shared schema, isolated by tenant_id).
 * All domain repositories must filter by TenantId to ensure data isolation.
 */
@Configuration
class MultiTenancyConfig
