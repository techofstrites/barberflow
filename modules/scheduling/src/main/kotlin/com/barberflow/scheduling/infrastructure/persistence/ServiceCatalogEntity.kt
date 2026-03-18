package com.barberflow.scheduling.infrastructure.persistence

import com.barberflow.scheduling.domain.model.ServiceItem
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "service_catalog")
class ServiceCatalogEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: BigDecimal,

    @Column(name = "duration_minutes", nullable = false)
    val durationMinutes: Int,

    @Column(nullable = false)
    val active: Boolean = true
) {
    fun toDomain() = ServiceItem(
        serviceId = id,
        name = name,
        price = price,
        durationMinutes = durationMinutes
    )
}
