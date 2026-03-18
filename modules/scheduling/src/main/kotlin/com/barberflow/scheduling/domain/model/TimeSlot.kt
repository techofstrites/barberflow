package com.barberflow.scheduling.domain.model

import java.time.ZonedDateTime

data class TimeSlot(
    val startAt: ZonedDateTime,
    val endAt: ZonedDateTime
) {
    init {
        require(endAt.isAfter(startAt)) { "endAt must be after startAt" }
    }

    fun overlapsWith(other: TimeSlot): Boolean =
        startAt.isBefore(other.endAt) && endAt.isAfter(other.startAt)

    fun durationMinutes(): Long =
        java.time.Duration.between(startAt, endAt).toMinutes()
}
