package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A favouriteDishes entry must name a dish that exists in the food file. */
class GUnknownFavouriteDish : LogSkippingSystemTest() {
    override val name = "GUnknownFavouriteDish"
    override val description = "A favourite dish name that exists nowhere in the food file is rejected."
    override val food = "gvalidation/s13_unknown_favourite_dish/food.json"
    override val restaurants = "gvalidation/s13_unknown_favourite_dish/restaurants.json"
    override val scenario = "gvalidation/s13_unknown_favourite_dish/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
