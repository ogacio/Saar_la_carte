package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val DRIVING_PREFIX = "[DEBUG] Delivery Driving"
private const val DECISION_PREFIX = "[DEBUG] Restaurant Decision"
private const val DECIDED_ON_RESTAURANT_1 = "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1."
private const val BOTH_TYPES_SCENARIO = "foh/f28/scenario_both_types.json"
private const val FOOD_RICE = "foh/food_rice.json"
private const val FOOD_TWO_TYPES = "foh/food_two_types.json"

/** F28: with equal rating differences the lowest restaurant id wins. */
class F28TieGoesToLowestId : LogSkippingSystemTest() {
    override val name = "F28TieGoesToLowestId"
    override val description = "Two restaurants with 0/0 ratings: the group decides on restaurant 1."
    override val food = FOOD_TWO_TYPES
    override val restaurants = "foh/f28/restaurants_tied.json"
    override val scenario = BOTH_TYPES_SCENARIO
    override val logLevel = DEBUG
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            DECISION_PREFIX,
            DECIDED_ON_RESTAURANT_1,
        )
    }
}

/** F28: the difference between positive and negative ratings decides, not the positive count. */
class F28HighestRatingDifferenceWins : LogSkippingSystemTest() {
    override val name = "F28HighestRatingDifferenceWins"
    override val description = "Restaurant 1 has 5/4, restaurant 2 has 3/0: the group decides on restaurant 2."
    override val food = FOOD_TWO_TYPES
    override val restaurants = "foh/f28/restaurants_rated.json"
    override val scenario = BOTH_TYPES_SCENARIO
    override val logLevel = DEBUG
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            DECISION_PREFIX,
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 2.",
        )
    }
}

/** F28: a restaurant does not accept new customers in the last 3 ticks of its opening time. */
class F28NoNewCustomersInLastThreeTicks : LogSkippingSystemTest() {
    override val name = "F28NoNewCustomersInLastThreeTicks"
    override val description = "Restaurant closes after tick 10; a group arriving at tick 8 finds no restaurant."
    override val food = FOOD_RICE
    override val restaurants = "foh/f28/restaurants_closing_at_ten.json"
    override val scenario = "foh/f28/scenario_arrives_at_eight.json"
    override val logLevel = DEBUG
    override val maxTicks = 8

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] Restaurant No Decision",
            "[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant.",
        )
    }
}

/** F20, F24, F29: a delivery over 7 km, from the order to the customers receiving their meals. */
class F20DeliveryOverSevenKilometres : LogSkippingSystemTest() {
    override val name = "F20DeliveryOverSevenKilometres"
    override val description = "Order without waitstaff, hand-over to driver 1, two driving ticks, arrival."
    override val food = FOOD_RICE
    override val restaurants = "foh/f20/restaurants_one_driver.json"
    override val scenario = "foh/f20/scenario_delivery_seven_km.json"
    override val logLevel = DEBUG
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            DECISION_PREFIX,
            DECIDED_ON_RESTAURANT_1,
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:2.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Delivery",
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:2 meals to driver 1 for order 1.",
        )
        skipToAndAssert(
            "[INFO] Delivery Preparation",
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 2 ticks.",
        )
        skipToAndAssert(
            DRIVING_PREFIX,
            "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.",
        )
        skipToAndAssert(
            DRIVING_PREFIX,
            "[DEBUG] Delivery Driving (R 1): Driver 1 drove 2 km and needs 0 more ticks.",
        )
        assertNextLine("[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.")
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.")
        skipToAndAssert(
            "[INFO] Delivery Finished Eating",
            "[INFO] Delivery Finished Eating (R 1): Group 1 has finished eating.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        assertStatistics(1, cooked = 2, served = 0, delivered = 2, ratings = 1)
    }
}
