package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24
private const val SERVES = "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves "

/**
 * F27 plus the rating likelihood, on the exact boundary of the expectation window.
 *
 * "An experience counts as positive if the food for the whole group arrives before the 4 tick
 * expectation window is over", and a neutral experience is one where it merely arrives in time.
 * "CASUAL customers with SOME ... will not rate after a neutral experience."
 *
 * Both groups order in tick 3 on a single EXEC cook. Group 1's Long Braise is 40 minutes, so it is
 * served 3 ticks after ordering, inside the window, and its SOME likelihood produces a rating.
 * Group 2's Quick Bowl waits behind it and is served exactly 4 ticks after ordering, which is
 * neutral, so group 2 is escorted without ever rating. The statistics line proves the absence: the
 * restaurant receives one rating for two groups.
 *
 * Moving the boundary either way flips one of the two groups, and a mutant that lets SOME rate on
 * neutral experiences turns the count into 2.
 */
class F27TheFourthTickIsNeutralSoSomeDoesNotRate : LogSkippingSystemTest() {
    override val name = "F27TheFourthTickIsNeutralSoSomeDoesNotRate"
    override val description =
        "Served in 3 ticks a SOME group rates, served in exactly 4 it does not: one rating for two groups."
    override val food = "delivery/browsing/food_three_durations.json"
    override val restaurants = "delivery/browsing/restaurants_two_tables_one_cook.json"
    override val scenario = "delivery/browsing/scenario_long_then_quick.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(SERVES, SERVES + "Long Braise:2 to table 1 3 ticks after ordering.")
        skipToAndAssert(SERVES, SERVES + "Quick Bowl:2 to table 2 4 ticks after ordering.")
        skipToAndAssert(
            "[INFO] Rating (R 1)",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        // Group 2 is escorted in the next tick, with no Rating line of its own.
        skipToAndAssert(
            "[IMPORTANT] FOH Escorting",
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 2 from table 2 outside.",
        )
        assertStatistics(restaurantId = 1, cooked = 4, served = 4, delivered = 0, ratings = 1)
    }
}
