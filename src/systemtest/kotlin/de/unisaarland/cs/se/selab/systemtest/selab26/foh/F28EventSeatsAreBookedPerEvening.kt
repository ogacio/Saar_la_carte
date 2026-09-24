package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val SIX_EVENINGS = 144

/**
 * F28: the browsing service keeps the booked event seats per event evening, not per restaurant.
 * The hall has 20 seats. Group 1 books 14 of them for evening 4, which leaves 6 - too few for
 * group 2's ten, so group 2 never decides on a restaurant at all. Group 3 wants the same ten seats
 * on evening 5, where nothing is booked, and is accepted.
 */
class F28EventSeatsAreBookedPerEvening : LogSkippingSystemTest() {
    override val name = "F28EventSeatsAreBookedPerEvening"
    override val description =
        "An event filling evening 4 blocks a second event that evening but not one on evening 5."
    override val food = "foh/f28_event_seats/food_two_dishes.json"
    override val restaurants = "foh/f28_event_seats/restaurants_twenty_seats.json"
    override val scenario = "foh/f28_event_seats/scenario_three_events.json"
    override val logLevel = INFO
    override val maxTicks = SIX_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] Restaurant Arrival",
            "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:14 with waitstaff 1,2.",
        )
        // Group 2 finds only six seats left on evening 4 and never arrives; the next arrival is
        // group 3, on evening 5.
        skipToAndAssert(
            "[INFO] Restaurant Arrival",
            "[INFO] Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 3 placed order 2 of Rice Bowl:10 with waitstaff 1.",
        )
        assertStatistics(restaurantId = 1, cooked = 24, served = 24, delivered = 0, ratings = 2)
    }
}
