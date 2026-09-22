package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val ONE_EVENING = 24
private const val DECISION_PREFIX = "[DEBUG] Restaurant Decision"

/** F28: the type filter precedes the rating comparison, even against a better-rated match. */
class F28TypeFilterPrecedesRating : LogSkippingSystemTest() {
    override val name = "F28TypeFilterPrecedesRating"
    override val description =
        "Restaurant 1 (ASIAN, diff 1) is the group's only listed type; restaurant 2 (EUROPEAN, diff 3) " +
            "rates higher but is filtered out, so the group decides on restaurant 1."
    override val food = "foh/food_two_types.json"
    override val restaurants = "foh/f28/restaurants_rated.json"
    override val scenario = "foh/f28d_indifference/scenario_asian_only.json"
    override val logLevel = DEBUG
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            DECISION_PREFIX,
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1.",
        )
    }
}
