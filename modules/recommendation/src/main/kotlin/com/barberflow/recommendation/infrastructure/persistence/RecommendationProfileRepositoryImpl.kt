package com.barberflow.recommendation.infrastructure.persistence

import com.barberflow.core.tenant.TenantId
import com.barberflow.recommendation.domain.model.RecommendationProfile
import com.barberflow.recommendation.domain.repository.RecommendationProfileRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class RecommendationProfileRepositoryImpl(
    private val jpa: RecommendationProfileJpaRepository
) : RecommendationProfileRepository {

    override fun save(profile: RecommendationProfile): RecommendationProfile {
        jpa.save(RecommendationProfileEntity.fromDomain(profile))
        return profile
    }

    override fun findByCustomerId(customerId: UUID, tenantId: TenantId): RecommendationProfile? =
        jpa.findById(customerId).orElse(null)
            ?.takeIf { it.tenantId == tenantId.value }
            ?.toDomain()

    override fun findAllActive(tenantId: TenantId): List<RecommendationProfile> =
        jpa.findAllByTenantId(tenantId.value).map { it.toDomain() }
}
