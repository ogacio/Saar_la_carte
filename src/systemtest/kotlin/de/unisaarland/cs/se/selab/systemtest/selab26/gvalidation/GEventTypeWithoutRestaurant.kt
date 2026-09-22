package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** An EVENT group's only restaurantType must have a matching favourite that is a basic dish of that type. */
class GEventTypeWithoutRestaurant : LogSkippingSystemTest() {
    override val name = "GEventTypeWithoutRestaurant"
    override val description = "An EVENT group whose only type is AFRICAN, with no AFRICAN restaurant " +
        "or basic dish, is rejected."
    override val food = "gvalidation/s12_event_type_without_restaurant/food.json"
    override val restaurants = "gvalidation/s12_event_type_without_restaurant/restaurants.json"
    override val scenario = "gvalidation/s12_event_type_without_restaurant/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
