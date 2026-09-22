package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** An excludedIngredients entry must name an ingredient that exists in the food file. */
class GUnknownExcludedIngredient : LogSkippingSystemTest() {
    override val name = "GUnknownExcludedIngredient"
    override val description = "An excluded ingredient name that exists nowhere in the food file is rejected."
    override val food = "gvalidation/s15_unknown_excluded_ingredient/food.json"
    override val restaurants = "gvalidation/s15_unknown_excluded_ingredient/restaurants.json"
    override val scenario = "gvalidation/s15_unknown_excluded_ingredient/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
