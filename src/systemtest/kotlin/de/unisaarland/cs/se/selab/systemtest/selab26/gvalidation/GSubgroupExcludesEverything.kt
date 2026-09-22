package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/** Same rule as GCasualExcludesEverything, on a REGULAR group's single foodPreference sub-group. */
class GSubgroupExcludesEverything : LogSkippingSystemTest() {
    override val name = "GSubgroupExcludesEverything"
    override val description = "A foodPreference sub-group excluding every ingredient in the food file is rejected."
    override val food = "gvalidation/s39_subgroup_excludes_everything/food.json"
    override val restaurants = "gvalidation/s39_subgroup_excludes_everything/restaurants.json"
    override val scenario = "gvalidation/s39_subgroup_excludes_everything/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}

/** The full-set rule applies per sub-group: one of two sub-groups excluding everything is enough to reject. */
class GTwoSubgroupsOneExcludesAll : LogSkippingSystemTest() {
    override val name = "GTwoSubgroupsOneExcludesAll"
    override val description = "Of two foodPreference sub-groups, one excluding every ingredient is rejected " +
        "even though the other is fine."
    override val food = "gvalidation/s39_two_subgroups_one_excludes_all/food.json"
    override val restaurants = "gvalidation/s39_two_subgroups_one_excludes_all/restaurants.json"
    override val scenario = "gvalidation/s39_two_subgroups_one_excludes_all/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Initialization Info:",
            "[IMPORTANT] Initialization Info: scenario.json is invalid.",
        )
    }
}
