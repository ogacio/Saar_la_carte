package de.unisaarland.cs.se.selab.systemtest.selab26.statistics

/* F07: what the four statistics lines count, and over which part of the simulation. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT_LEVEL = "IMPORTANT"
private const val FOOD_RICE = "f07/food_rice.json"
private const val EVENING_TICKS = 24

/** F07: delivered counts customers, so one order for three people counts as three. */
class F07DeliveredCountsCustomersNotOrders : LogSkippingSystemTest() {
    override val name = "F07DeliveredCountsCustomersNotOrders"
    override val description = "One delivery order for three customers counts as three delivered customers."
    override val food = FOOD_RICE
    override val restaurants = "f07/restaurants_driver_and_table.json"
    override val scenario = "f07/scenario_delivery_group_of_three.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = EVENING_TICKS

    override suspend fun run() {
        assertStatistics(1, cooked = 3, served = 0, delivered = 3, ratings = 1)
    }
}

/** F07: a group of four is four meals, four served customers and a single rating. */
class F07RatingsCountRatingsNotCustomers : LogSkippingSystemTest() {
    override val name = "F07RatingsCountRatingsNotCustomers"
    override val description = "A group of four is four cooked meals, four served customers and one rating."
    override val food = FOOD_RICE
    override val restaurants = "f07/restaurants_table_of_four.json"
    override val scenario = "f07/scenario_group_of_four.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = EVENING_TICKS

    override suspend fun run() {
        assertStatistics(1, cooked = 4, served = 4, delivered = 0, ratings = 1)
    }
}

/** F07: the statistics cover the whole simulation, so two evenings add up. */
class F07CountsAddUpOverTheWholeSimulation : LogSkippingSystemTest() {
    override val name = "F07CountsAddUpOverTheWholeSimulation"
    override val description = "The same group on two evenings is counted twice, not reset between evenings."
    override val food = FOOD_RICE
    override val restaurants = "f07/restaurants_table_of_four.json"
    override val scenario = "f07/scenario_group_of_four_two_evenings.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = EVENING_TICKS * 2 + 2

    override suspend fun run() {
        assertStatistics(1, cooked = 8, served = 8, delivered = 0, ratings = 2)
    }
}

/** F07: each restaurant reports its own numbers, in ascending restaurant id. */
class F07EachRestaurantCountsOnlyItsOwn : LogSkippingSystemTest() {
    override val name = "F07EachRestaurantCountsOnlyItsOwn"
    override val description = "Two busy restaurants report their own numbers, the lower id first."
    override val food = "f07/food_two_types.json"
    override val restaurants = "f07/restaurants_two_types.json"
    override val scenario = "f07/scenario_one_group_per_type.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = EVENING_TICKS

    override suspend fun run() {
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 1)
        assertStatistics(2, cooked = 4, served = 4, delivered = 0, ratings = 1)
        assertEnd()
    }
}

/** F07: a group that is sent away adds a rating and nothing else. */
class F07SentAwayGroupIsARatingButNotACustomer : LogSkippingSystemTest() {
    override val name = "F07SentAwayGroupIsARatingButNotACustomer"
    override val description = "A group sent away for lack of a table adds a rating and nothing else."
    override val food = FOOD_RICE
    override val restaurants = "f07/restaurants_bar_only.json"
    override val scenario = "f07/scenario_bar_group_of_three.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = EVENING_TICKS

    override suspend fun run() {
        assertStatistics(1, cooked = 0, served = 0, delivered = 0, ratings = 1)
    }
}
