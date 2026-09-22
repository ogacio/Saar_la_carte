package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A REGULAR group's restaurant id must reference an existing restaurant. */
class GRegularUnknownRestaurant : LogSkippingSystemTest() {
    override val name = "GRegularUnknownRestaurant"
    override val description = "A REGULAR group bound to restaurant 99, which does not exist, is rejected."
    override val food = "gvalidation/s05_regular_unknown_restaurant/food.json"
    override val restaurants = "gvalidation/s05_regular_unknown_restaurant/restaurants.json"
    override val scenario = "gvalidation/s05_regular_unknown_restaurant/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
