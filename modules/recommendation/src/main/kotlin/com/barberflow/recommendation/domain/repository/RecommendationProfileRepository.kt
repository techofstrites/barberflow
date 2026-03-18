package com.barberflow.recommendation.domain.repository

import com.barberflow.core.tenant.TenantId
import com.barberflow.recommendation.domain.model.RecommendationProfile
import java.util.UUID

interface RecommendationProfileRepository {
    fun save(profile: RecommendationProfile): RecommendationProfile
    fun findByCustomerId(customerId: UUID, tenantId: TenantId): RecommendationProfile?
    fun findAllActive(tenantId: TenantId): List<RecommendationProfile>
}
