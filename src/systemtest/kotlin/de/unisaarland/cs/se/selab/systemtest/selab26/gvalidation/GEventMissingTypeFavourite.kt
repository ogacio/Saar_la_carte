package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** An EVENT group must name a favourite dish for every one of its considered restaurantTypes. */
class GEventMissingTypeFavourite : LogSkippingSystemTest() {
    override val name = "GEventMissingTypeFavourite"
    override val description = "An EVENT group missing a favourite dish for one of its restaurantTypes is rejected."
    override val food = "gvalidation/s10_event_missing_type_favourite/food.json"
    override val restaurants = "gvalidation/s10_event_missing_type_favourite/restaurants.json"
    override val scenario = "gvalidation/s10_event_missing_type_favourite/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
