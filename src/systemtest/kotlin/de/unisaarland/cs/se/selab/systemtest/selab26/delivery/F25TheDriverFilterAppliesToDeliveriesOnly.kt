package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * The free-driver filter is part of the delivery branch of browsing only; a group walking in is
 * ranked on seats instead. Alpha (restaurant 1) is five positive ratings ahead but has no delivery
 * service at all, Beta (restaurant 2) has one driver and no ratings.
 *
 * Both groups have visitingTick 10. The delivery group decides in tick 10 - 1 - 3 = 6 and has to
 * take Beta, the only restaurant that can deliver at all. The walk-in group decides in tick 10 and
 * takes Alpha, because for it the drivers are irrelevant and the rating decides.
 *
 * One mutant applies the driver filter to everyone, which sends the walk-in group to Beta; another
 * drops it entirely, which sends the delivery to driverless Alpha. Either way one of the two lines
 * below names the wrong restaurant.
 */
class F25TheDriverFilterAppliesToDeliveriesOnly : LogSkippingSystemTest() {
    override val name = "F25TheDriverFilterAppliesToDeliveriesOnly"
    override val description =
        "A delivery takes the only restaurant with a driver while a walk-in takes the better-rated " +
            "one that has none."
    override val food = "delivery/browsing/food_two_dishes.json"
    override val restaurants = "delivery/browsing/restaurants_rated_no_driver.json"
    override val scenario = "delivery/browsing/scenario_delivery_and_dinein.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 2): Group 1 placed order 1 of Rice Bowl:2.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Seating",
            "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 1 by waitstaff 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:2 with waitstaff 1.",
        )
    }
}
