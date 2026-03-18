package com.barberflow.recommendation.application

import com.barberflow.recommendation.domain.repository.RecommendationProfileRepository
import com.barberflow.core.tenant.TenantId
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RetentionCheckJob(
    private val profileRepository: RecommendationProfileRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val activeTenants = mutableSetOf<String>()

    fun registerTenant(tenantId: String) = activeTenants.add(tenantId)

    @Scheduled(cron = "0 0 8 * * *") // daily at 8am
    fun checkRetentionWindows() {
        val today = java.time.LocalDate.now()
        log.info("Running retention check for {} tenants", activeTenants.size)

        activeTenants.forEach { tenantIdStr ->
            try {
                val tenantId = TenantId.from(tenantIdStr)
                val profiles = profileRepository.findAllActive(tenantId)
                profiles.forEach { profile ->
                    profile.checkAndTriggerRetention(today)
                    if (profile.domainEvents.isNotEmpty()) {
                        profileRepository.save(profile)
                        profile.domainEvents.forEach { eventPublisher.publishEvent(it) }
                        profile.clearEvents()
                    }
                }
                log.info("Checked {} profiles for tenant {}", profiles.size, tenantIdStr)
            } catch (e: Exception) {
                log.error("Retention check failed for tenant {}: {}", tenantIdStr, e.message)
            }
        }
    }
}
