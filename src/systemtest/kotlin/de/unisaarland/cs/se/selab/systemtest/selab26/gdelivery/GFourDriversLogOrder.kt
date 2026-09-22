package de.unisaarland.cs.se.selab.systemtest.selab26.gdelivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/**
 * From tests/giant/gaps/g4_drivers: four deliveries across two restaurants complete in one tick.
 * Locks in Post 19's per-log-type ordering, ascending group id: all Driving, then all Arrival,
 * then all Finished, within each restaurant. Fixed by Constantin's `DeliveryDriverChanges`
 * (commits `9270bb3`/`d13f36c`, 2026-09-21) — see tests/giant/BUGS-FOUND.md #4. This test
 * previously locked in the opposite (buggy, per-driver) order and flipped to FAIL the moment that
 * fix landed, exactly as its old KDoc predicted; rewritten here to the now-correct order.
 */
class GFourDriversLogOrder : LogSkippingSystemTest() {
    override val name = "GFourDriversLogOrder"
    override val description = "Four drivers across two restaurants finish in one tick; logs group " +
        "per log type, ascending group id, per Post 19."
    override val food = "gdelivery/g4_drivers/food.json"
    override val restaurants = "gdelivery/g4_drivers/restaurants.json"
    override val scenario = "gdelivery/g4_drivers/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 48

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] Delivery Arrival (R 1): Driver 2 arrived at group 1 with order 3.",
            "[INFO] Delivery Arrival (R 1): Driver 2 arrived at group 1 with order 3.",
        )
        assertNextLine("[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 2 with order 2.")
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 2 gave delivery of order 3 to group 1.")
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 2 to group 2.")
    }
}
