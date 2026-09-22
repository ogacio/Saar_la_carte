package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/**
 * A CASUAL delivery group's visitingTick must leave enough ticks for travel plus cooking:
 * visitingTick - travelTicks - 3 >= 1.
 */
class GCasualDeliveryTickTooEarly : LogSkippingSystemTest() {
    override val name = "GCasualDeliveryTickTooEarly"
    override val description = "A CASUAL delivery group at distance 5, visitingTick 4, is rejected " +
        "for too little travel time."
    override val food = "gvalidation/s08_casual_delivery_tick_too_early/food.json"
    override val restaurants = "gvalidation/s08_casual_delivery_tick_too_early/restaurants.json"
    override val scenario = "gvalidation/s08_casual_delivery_tick_too_early/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
