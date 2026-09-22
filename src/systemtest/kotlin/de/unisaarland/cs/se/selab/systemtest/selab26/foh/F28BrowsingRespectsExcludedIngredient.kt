package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val ONE_EVENING = 24
private const val DECISION_PREFIX = "[DEBUG] Restaurant Decision"

/** F28: an all-excluded menu at the better-rated restaurant is dropped from browsing. */
class F28BrowsingRespectsExcludedIngredient : LogSkippingSystemTest() {
    override val name = "F28BrowsingRespectsExcludedIngredient"
    override val description =
        "Restaurant 1 rates higher but its only dish contains the group's excluded shrimp; " +
            "the group decides on restaurant 2, whose Rice Bowl is edible."
    override val food = "foh/f28c_freeforall/food_two_dishes.json"
    override val restaurants = "foh/f28c_freeforall/restaurants_one_all_excluded.json"
    override val scenario = "foh/f28c_freeforall/scenario_excludes_shrimp.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            DECISION_PREFIX,
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 2.",
        )
    }
}
