package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val FOUR_EVENINGS = 96

/**
 * P05: "EVENT groups always leave a rating", while "CASUAL customers with SOME ... will not rate
 * after a neutral experience". Both happen in one evening here: the event is served in the tick it
 * orders and rates, the casual group waits for the same cook and is served exactly four ticks after
 * ordering - in time, so neutral - and leaves without rating. The statistics line carries the
 * proof: two groups, one rating.
 */
class P05AnEventAlwaysRatesWhereSomeStaysSilent : LogSkippingSystemTest() {
    override val name = "P05AnEventAlwaysRatesWhereSomeStaysSilent"
    override val description =
        "An EVENT group rates its visit while a SOME casual served at the neutral boundary does not."
    override val food = "foh/p05_event_always_rates/food_slow_and_quick.json"
    override val restaurants = "foh/p05_event_always_rates/restaurants_one_cook.json"
    override val scenario = "foh/p05_event_always_rates/scenario_event_served_late.json"
    override val logLevel = INFO
    override val maxTicks = FOUR_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Serving",
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:6 to table 1 0 ticks after ordering.",
        )
        skipToAndAssert(
            "[INFO] Rating (R 1)",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Serving",
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Slow Braise:2 to table 2 4 ticks after ordering.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Escorting",
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 1 from table 2 outside.",
        )
        assertStatistics(restaurantId = 1, cooked = 8, served = 8, delivered = 0, ratings = 1)
    }
}
