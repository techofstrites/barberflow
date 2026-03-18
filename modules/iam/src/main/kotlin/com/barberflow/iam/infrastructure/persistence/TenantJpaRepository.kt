package com.barberflow.iam.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantJpaRepository : JpaRepository<TenantEntity, UUID> {
    fun findBySlug(slug: String): TenantEntity?
    fun existsBySlug(slug: String): Boolean
}
