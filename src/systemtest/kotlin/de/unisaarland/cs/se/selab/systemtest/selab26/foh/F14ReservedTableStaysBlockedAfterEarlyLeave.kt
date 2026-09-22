package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val ONE_EVENING = 24

/** F14: a table reserved for a REGULAR stays reserved even after that group leaves early. */
class F14ReservedTableStaysBlockedAfterEarlyLeave : LogSkippingSystemTest() {
    override val name = "F14ReservedTableStaysBlockedAfterEarlyLeave"
    override val description =
        "The only table is reserved for group 1, which leaves at tick 1; group 2 still cannot decide later."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f14b_denkmalschutz/restaurants_one_table.json"
    override val scenario = "foh/f14b_denkmalschutz/scenario_regular_then_casual.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Seating",
            "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.",
        )
        skipToAndAssert(
            "[DEBUG] Restaurant No Decision",
            "[DEBUG] Restaurant No Decision: Group 2 could not decide for a restaurant.",
        )
    }
}
