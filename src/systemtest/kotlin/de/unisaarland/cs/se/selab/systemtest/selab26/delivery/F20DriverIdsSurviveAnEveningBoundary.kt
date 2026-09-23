package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val TWO_EVENINGS = 48

/**
 * F20: a driver still RETURNING when its evening closes must keep its RETURNING state and
 * shed its id exactly once on arrival, so the next evening's second driver is granted a
 * distinct id instead of colliding with the stale one (regression for the double
 * `resetForEvening()` call fixed in `d25ba0a`).
 */
class F20DriverIdsSurviveAnEveningBoundary : LogSkippingSystemTest() {
    override val name = "F20DriverIdsSurviveAnEveningBoundary"
    override val description =
        "Group 1's 40 km delivery returns after evening 1 closes; evening 2's two deliveries " +
            "must be handled by two distinct driver ids, not one colliding id."
    override val food = "f20/f20b_cross_evening_return/food_rice.json"
    override val restaurants = "f20/f20b_cross_evening_return/restaurants_two_drivers.json"
    override val scenario = "f20/f20b_cross_evening_return/scenario_driver_returns_next_evening.json"
    override val logLevel = DEBUG
    override val maxTicks = TWO_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 1 (2)",
            "[IMPORTANT] Simulation: Tick 1 (2) started.",
        )
        skipToAndAssert(
            "[INFO] Delivery Returned",
            "[INFO] Delivery Returned (R 1): Driver 1 has returned.",
        )
        skipToAndAssert(
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order",
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 2 to group 2, " +
                "which will take 1 ticks.",
        )
        skipToAndAssert(
            "[INFO] Delivery Preparation (R 1): Driver 2 prepares driving order",
            "[INFO] Delivery Preparation (R 1): Driver 2 prepares driving order 3 to group 3, " +
                "which will take 1 ticks.",
        )
        assertStatistics(1, cooked = 6, served = 0, delivered = 6, ratings = 3)
    }
}
