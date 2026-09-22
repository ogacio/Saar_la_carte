package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** A foodPreference may not exclude every ingredient in the food file: nothing would be orderable. */
class GCasualExcludesEverything : LogSkippingSystemTest() {
    override val name = "GCasualExcludesEverything"
    override val description = "A CASUAL group's foodPreference excluding every ingredient in the " +
        "food file is rejected."
    override val food = "gvalidation/s36_casual_excludes_everything/food.json"
    override val restaurants = "gvalidation/s36_casual_excludes_everything/restaurants.json"
    override val scenario = "gvalidation/s36_casual_excludes_everything/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
