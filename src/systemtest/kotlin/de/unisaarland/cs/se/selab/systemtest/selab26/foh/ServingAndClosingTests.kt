package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val INFO = "INFO"
private const val IMPORTANT = "IMPORTANT"
private const val SERVING_PREFIX = "[IMPORTANT] FOH Serving"
private const val RATING_PREFIX = "[INFO] Rating"
private const val FOOD_ONE_COOK = "foh/food_one_cook.json"
private const val ONE_COOK = "foh/p05/restaurants_one_cook.json"
private const val SERVED_AFTER_FOUR_TICKS =
    "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Quick Salad:1,Slow Stew:1 to table 1 4 ticks after ordering."

/** F18: the pantry covers one portion only, so the second customer cannot order and leaves. */
class F18SecondCustomerFindsNoDishAndLeaves : LogSkippingSystemTest() {
    override val name = "F18SecondCustomerFindsNoDishAndLeaves"
    override val description = "Planning buys one 5-package for a 3-unit dish: one order, one customer leaves."
    override val food = "foh/food_scarce.json"
    override val restaurants = "foh/f18/restaurants_scarce.json"
    override val scenario = "foh/f18/scenario_two_customers_one_portion.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Tofu Bowl:1 with waitstaff 1.",
        )
        assertNextLine(
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 1 customers, " +
                "they leave the restaurant.",
        )
    }
}

/** F19: the table is held back while the second meal is still cooking and then served in one go. */
class F19TableHeldBackUntilAllMealsAreCooked : LogSkippingSystemTest() {
    override val name = "F19TableHeldBackUntilAllMealsAreCooked"
    override val description = "Fast dish cooked at once, slow dish one tick later: no serving first, then both."
    override val food = "foh/food_fast_slow.json"
    override val restaurants = "foh/f19/restaurants_exec_and_sous.json"
    override val scenario = "foh/f19/scenario_fast_and_slow.json"
    override val logLevel = DEBUG
    override val maxTicks = 4

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] FOH No Serving",
            "[DEBUG] FOH No Serving (R 1): Waitstaff 1 did not serve 1 meals to table 1.",
        )
        skipToAndAssert(
            SERVING_PREFIX,
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Fast Noodles:1,Slow Stew:1 to table 1 " +
                "1 ticks after ordering.",
        )
    }
}

/** P05: food exactly at the end of the 4-tick window is neutral; a SOME group does not rate it. */
class P05SomeLikelihoodSkipsNeutralExperience : LogSkippingSystemTest() {
    override val name = "P05SomeLikelihoodSkipsNeutralExperience"
    override val description = "One cook serializes a 3-tick and a 0-tick dish: served after 4 ticks, no rating."
    override val food = FOOD_ONE_COOK
    override val restaurants = ONE_COOK
    override val scenario = "foh/p05/scenario_neutral_some.json"
    override val logLevel = INFO
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(SERVING_PREFIX, SERVED_AFTER_FOUR_TICKS)
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 0)
    }
}

/** P05: the same neutral experience is rated POSITIVE by an ALWAYS group. */
class P05AlwaysLikelihoodRatesNeutralPositive : LogSkippingSystemTest() {
    override val name = "P05AlwaysLikelihoodRatesNeutralPositive"
    override val description = "Served after exactly 4 ticks, the ALWAYS group leaves a POSITIVE rating."
    override val food = FOOD_ONE_COOK
    override val restaurants = ONE_COOK
    override val scenario = "foh/p05/scenario_neutral_always.json"
    override val logLevel = INFO
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(SERVING_PREFIX, SERVED_AFTER_FOUR_TICKS)
        skipToAndAssert(
            RATING_PREFIX,
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
    }
}

/** F30: a group still eating when the opening time ends is sent out and rates negatively. */
class F30GroupStillEatingAtClosingRatesNegative : LogSkippingSystemTest() {
    override val name = "F30GroupStillEatingAtClosingRatesNegative"
    override val description = "Served in the last opening tick, the group is escorted out unfinished: NEGATIVE."
    override val food = "foh/food_slow.json"
    override val restaurants = "foh/f30/restaurants_closing_at_six.json"
    override val scenario = "foh/f30/scenario_arrives_at_three.json"
    override val logLevel = INFO
    override val maxTicks = 8

    override suspend fun run() {
        skipToAndAssert(
            RATING_PREFIX,
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}

/** F07: served and delivered customers are counted separately in one run. */
class F07ServedAndDeliveredCountedSeparately : LogSkippingSystemTest() {
    override val name = "F07ServedAndDeliveredCountedSeparately"
    override val description = "An eat-in group of 2 and a delivery group of 1: 3 cooked, 2 served, 1 delivered."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f07/restaurants_table_and_driver.json"
    override val scenario = "foh/f07/scenario_eat_in_and_delivery.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 24

    override suspend fun run() {
        assertStatistics(1, cooked = 3, served = 2, delivered = 1, ratings = 2)
    }
}

/** P03: an EVENT group larger than one waiter's SEATING limit is seated by two waiters in the same tick. */
class P03EventSeatedByTwoWaiters : LogSkippingSystemTest() {
    override val name = "P03EventSeatedByTwoWaiters"
    override val description = "Event of 12 with two waiters: waitstaff 1 seats 10, waitstaff 2 seats 2."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/p03/restaurants_two_waiters.json"
    override val scenario = "foh/p03/scenario_event_of_twelve.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 77

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Seating",
            "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1,2.",
        )
    }
}
