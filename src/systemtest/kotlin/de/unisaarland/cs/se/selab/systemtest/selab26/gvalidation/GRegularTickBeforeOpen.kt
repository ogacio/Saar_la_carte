package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A REGULAR group's visitingTick must be at or after openingTickStart. */
class GRegularTickBeforeOpen : LogSkippingSystemTest() {
    override val name = "GRegularTickBeforeOpen"
    override val description = "A REGULAR group's visitingTick 4 is rejected when the bound restaurant opens at 5."
    override val food = "gvalidation/s03_regular_tick_before_open/food.json"
    override val restaurants = "gvalidation/s03_regular_tick_before_open/restaurants.json"
    override val scenario = "gvalidation/s03_regular_tick_before_open/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
