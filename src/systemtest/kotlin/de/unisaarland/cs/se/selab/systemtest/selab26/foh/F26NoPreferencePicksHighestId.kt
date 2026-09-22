package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"

/** F26: a group with no preferences at all orders the highest-id orderable dish. */
class F26NoPreferencePicksHighestId : LogSkippingSystemTest() {
    override val name = "F26NoPreferencePicksHighestId"
    override val description = "A group without preferences orders Tofu Rice (id 2), not Rice Bowl (id 1)."
    override val food = "foh/food_menu.json"
    override val restaurants = "foh/f18/restaurants_two_dishes.json"
    override val scenario = "foh/f26_kingofthehill/scenario_no_preference_group.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Tofu Rice:2 with waitstaff 1.",
        )
    }
}
