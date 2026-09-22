package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** Customer group ids must be unique across the scenario file. */
class GDuplicateGroupId : LogSkippingSystemTest() {
    override val name = "GDuplicateGroupId"
    override val description = "Two customer groups sharing the same id are rejected."
    override val food = "gvalidation/s06_duplicate_group_id/food.json"
    override val restaurants = "gvalidation/s06_duplicate_group_id/restaurants.json"
    override val scenario = "gvalidation/s06_duplicate_group_id/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
