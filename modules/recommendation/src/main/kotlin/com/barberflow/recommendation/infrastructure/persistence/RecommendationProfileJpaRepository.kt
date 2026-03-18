package com.barberflow.recommendation.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RecommendationProfileJpaRepository : JpaRepository<RecommendationProfileEntity, UUID> {
    fun findAllByTenantId(tenantId: UUID): List<RecommendationProfileEntity>
}
