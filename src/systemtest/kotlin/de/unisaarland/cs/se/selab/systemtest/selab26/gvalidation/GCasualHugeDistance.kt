package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A deliveryDistance so large that no visitingTick leaves enough time for travel plus cooking is rejected. */
class GCasualHugeDistance : LogSkippingSystemTest() {
    override val name = "GCasualHugeDistance"
    override val description = "A CASUAL delivery group with deliveryDistance 1000 is rejected for " +
        "the same timing rule as GCasualDeliveryTickTooEarly."
    override val food = "gvalidation/s27_casual_huge_distance/food.json"
    override val restaurants = "gvalidation/s27_casual_huge_distance/restaurants.json"
    override val scenario = "gvalidation/s27_casual_huge_distance/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
