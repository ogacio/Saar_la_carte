package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** The sum of foodPreference sub-group sizes must not exceed the customer group's total size. */
class GSubgroupSumExceeds : LogSkippingSystemTest() {
    override val name = "GSubgroupSumExceeds"
    override val description = "foodPreference sub-group sizes summing above the group's own size are rejected."
    override val food = "gvalidation/s14_subgroup_sum_exceeds/food.json"
    override val restaurants = "gvalidation/s14_subgroup_sum_exceeds/restaurants.json"
    override val scenario = "gvalidation/s14_subgroup_sum_exceeds/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
