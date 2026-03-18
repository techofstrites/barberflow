package com.barberflow.recommendation.domain.model

data class RetentionWindow(
    val minDays: Int,
    val maxDays: Int
) {
    init {
        require(minDays > 0) { "minDays must be > 0" }
        require(maxDays >= minDays) { "maxDays must be >= minDays" }
    }

    fun isWithinWindow(daysSinceLastVisit: Long): Boolean =
        daysSinceLastVisit in minDays..maxDays

    companion object {
        val DEFAULT = RetentionWindow(minDays = 20, maxDays = 30)
    }
}
