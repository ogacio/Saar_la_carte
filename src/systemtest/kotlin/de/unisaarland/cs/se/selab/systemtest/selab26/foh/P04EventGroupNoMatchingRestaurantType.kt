package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val FIVE_TICKS = 5

/**
 * P04: an EVENT group that considers no restaurant type any restaurant actually has never
 * decides on one, three evenings ahead of its event, and this is logged as a customer-side
 * decision failure distinct from a table/seating problem.
 */
class P04EventGroupNoMatchingRestaurantType : LogSkippingSystemTest() {
    override val name = "P04EventGroupNoMatchingRestaurantType"
    override val description =
        "An EVENT group considering only AFRICAN restaurants finds none among ASIAN restaurants and never decides."
    override val food = "foh/food_menu.json"
    override val restaurants = "foh/p03/restaurants_event_two_dishes.json"
    override val scenario = "p04/scenario_event_no_matching_type.json"
    override val logLevel = DEBUG
    override val maxTicks = FIVE_TICKS

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] Restaurant No Decision",
            "[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant.",
        )
    }
}
