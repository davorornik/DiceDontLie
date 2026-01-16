package si.ornik.dicedontlie.data


data class DieRoll(
    val redDie: Int,
    val yellowDie: Int,
    val eventDie: EventDie? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing aggregated roll distribution counts.
 * Used to efficiently retrieve statistics without loading all roll data.
 */
data class RollDistribution(
    val sum: Int,
    val eventDie: EventDie?,
    val count: Int
)

/**
 * Data class representing simple roll distribution counts (without event die).
 * Used for regular games without an event die.
 */
data class RollDistributionSimple(
    val sum: Int,
    val count: Int
)

/**
 * Data class for number frequency information.
 */
data class NumberFrequency(
    val sum: Int,
    val count: Int,
    val percentage: Float
)

/**
 * Data class for roll distribution with percentage.
 */
data class RollDistributionWithPercentage(
    val sum: Int,
    val count: Int,
    val percentage: Float
)