package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** An ingredient may not be both excluded and preferred in the same foodPreference. */
class GExcludedAndPreferredSame : LogSkippingSystemTest() {
    override val name = "GExcludedAndPreferredSame"
    override val description = "The same ingredient listed as both excluded and preferred is rejected."
    override val food = "gvalidation/s24_excluded_and_preferred_same/food.json"
    override val restaurants = "gvalidation/s24_excluded_and_preferred_same/restaurants.json"
    override val scenario = "gvalidation/s24_excluded_and_preferred_same/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
