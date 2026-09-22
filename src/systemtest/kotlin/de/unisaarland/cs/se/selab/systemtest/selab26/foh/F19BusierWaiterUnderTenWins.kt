package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val ONE_EVENING = 24
private const val SEATING_PREFIX = "[IMPORTANT] FOH Seating"

/** F19: among waiters with current load below 10, the assignment cascade picks the busiest one. */
class F19BusierWaiterUnderTenWins : LogSkippingSystemTest() {
    override val name = "F19BusierWaiterUnderTenWins"
    override val description =
        "Waiter 1 (load 8) and waiter 2 (load 5) are both under ten; group 3 goes to the busier waiter 1."
    override val food = "foh/f19b_stonks/food_two_dishes.json"
    override val restaurants = "foh/f19b_stonks/restaurants_two_waiters.json"
    override val scenario = "foh/f19b_stonks/scenario_busier_waiter_wins.json"
    override val logLevel = IMPORTANT
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.",
        )
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 2.",
        )
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 3 by waitstaff 1.",
        )
    }
}
