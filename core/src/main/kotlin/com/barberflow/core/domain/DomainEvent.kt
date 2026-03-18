package com.barberflow.core.domain

import java.time.Instant
import java.util.UUID

interface DomainEvent {
    val eventId: UUID
    val occurredAt: Instant
    val aggregateId: String
}
