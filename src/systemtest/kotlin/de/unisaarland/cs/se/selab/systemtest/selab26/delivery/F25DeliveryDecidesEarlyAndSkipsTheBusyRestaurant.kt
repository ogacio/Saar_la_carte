package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * Two browsing rules that only deliveries exercise, in one evening.
 *
 * Both groups have visitingTick 12 but different distances, so they decide in different ticks:
 * "Those customers wanting deliveries decide on a restaurant and make their order early. They
 * factor in the delivery distance and add 3 ticks for cooking."
 *
 *  * group 1, 20 km = 4 travel ticks, decides in tick 12 - 4 - 3 = 5
 *  * group 2, 5 km = 1 travel tick, decides in tick 12 - 1 - 3 = 8
 *
 * In tick 5 both restaurants are idle, so group 1 takes the better-rated Beta (restaurant 2, five
 * positive ratings against Alpha's none). By tick 8 Beta's only driver is carrying that order, so
 * "they also store how many delivery drivers are currently available" removes Beta from group 2's
 * candidates and it falls back to the worse-rated Alpha.
 *
 * A mutant that decides at the visitingTick, that ignores the rating when ranking, or that keeps
 * offering a restaurant whose drivers are all out, changes which restaurant logs which order.
 */
class F25DeliveryDecidesEarlyAndSkipsTheBusyRestaurant : LogSkippingSystemTest() {
    override val name = "F25DeliveryDecidesEarlyAndSkipsTheBusyRestaurant"
    override val description =
        "Two deliveries with the same visitingTick decide 3 ticks apart; the later one skips the " +
            "better-rated restaurant because its only driver is out."
    override val food = "delivery/browsing/food_two_dishes.json"
    override val restaurants = "delivery/browsing/restaurants_two_asian.json"
    override val scenario = "delivery/browsing/scenario_two_distances.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        // The 20 km group decides first, and picks the better-rated restaurant 2.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 5 ",
            "[IMPORTANT] Simulation: Tick 5 (1) started.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 2): Group 1 placed order 1 of Rice Bowl:2.",
        )
        // Three ticks later the 5 km group finds restaurant 2 without a free driver.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 8 ",
            "[IMPORTANT] Simulation: Tick 8 (1) started.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:2.",
        )
    }
}
