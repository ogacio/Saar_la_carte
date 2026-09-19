package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

/* F24, F28, F29 and F30: who is offered a restaurant, who drives, and what survives closing time. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"
private const val IMPORTANT_LEVEL = "IMPORTANT"
private const val NO_DECISION = "[DEBUG] Restaurant No Decision"
private const val PREPARATION = "[INFO] Delivery Preparation"
private const val FINISHED = "[IMPORTANT] Delivery Finished"
private const val DECISION = "[DEBUG] Restaurant Decision"
private const val SEATING = "[IMPORTANT] FOH Seating"
private const val GROUP_1_SEATED = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."
private const val F28_FOOD = "f28/food_rice.json"
private const val F30_FOOD = "f30/food_rice.json"
private const val ORDER_1_TO_GROUP_1 =
    "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks."
private const val GROUP_2_UNDECIDED = "[DEBUG] Restaurant No Decision: Group 2 could not decide for a restaurant."

/** F24: five km is one driving tick, then two ticks of eating and a rating. */
class F24ADeliveredGroupEatsAndRates : LogSkippingSystemTest() {
    override val name = "F24ADeliveredGroupEatsAndRates"
    override val description = "A delivery of two meals is received, eaten and rated, and counted as delivered."
    override val food = "f24/food_rice.json"
    override val restaurants = "f24/restaurants_one_driver.json"
    override val scenario = "f24/scenario_one_delivery.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            PREPARATION,
            ORDER_1_TO_GROUP_1,
        )
        skipToAndAssert(
            "[INFO] Delivery Arrival",
            "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.",
        )
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.")
        skipToAndAssert(
            "[INFO] Delivery Finished Eating",
            "[INFO] Delivery Finished Eating (R 1): Group 1 has finished eating.",
        )
        assertStatistics(1, cooked = 2, served = 0, delivered = 2, ratings = 1)
    }
}

/** F24: a group gives up 3 ticks after the tick it wanted its food, and later attempts fail. */
class F24AGroupGivesUpOnALateDelivery : LogSkippingSystemTest() {
    override val name = "F24AGroupGivesUpOnALateDelivery"
    override val description = "The group whose meals are cooked too late gives up, and the later attempt fails."
    override val food = "f24/food_two_slow_dishes.json"
    override val restaurants = "f24/restaurants_one_cook_one_driver.json"
    override val scenario = "f24/scenario_eat_in_blocks_the_delivery.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] Delivery Given Up",
            "[INFO] Delivery Given Up (R 1): Group 2 gave up on waiting for delivery of order 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] Delivery Failed",
            "[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order 1 to group 2.",
        )
    }
}

/** F28: a delivery needs a free driver, and deciding for the restaurant takes one. */
class F28ADeliveryIsOnlyOfferedWhileADriverIsFree : LogSkippingSystemTest() {
    override val name = "F28ADeliveryIsOnlyOfferedWhileADriverIsFree"
    override val description = "Of two delivery groups in one tick, the second is not offered the restaurant."
    override val food = F28_FOOD
    override val restaurants = "f28/restaurants_one_driver.json"
    override val scenario = "f28/scenario_two_deliveries.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            DECISION,
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1.",
        )
        skipToAndAssert(NO_DECISION, GROUP_2_UNDECIDED)
    }
}

/** F28: only a restaurant that is currently open is offered to a casual group. */
class F28AClosedRestaurantIsNotOffered : LogSkippingSystemTest() {
    override val name = "F28AClosedRestaurantIsNotOffered"
    override val description = "One tick before opening nothing is offered, in the opening tick it is."
    override val food = F28_FOOD
    override val restaurants = "f28/restaurants_opens_at_five.json"
    override val scenario = "f28/scenario_before_and_at_opening.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            NO_DECISION,
            "[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant.",
        )
        skipToAndAssert(
            DECISION,
            "[DEBUG] Restaurant Decision: Group 2 decided on restaurant 1.",
        )
    }
}

/** F28: seats are reduced per decision, so the third group of the tick finds none free. */
class F28SeatsAreReducedAsEachGroupDecides : LogSkippingSystemTest() {
    override val name = "F28SeatsAreReducedAsEachGroupDecides"
    override val description = "Three groups, two tables: the third is not offered the restaurant."
    override val food = F28_FOOD
    override val restaurants = "f28/restaurants_two_tables.json"
    override val scenario = "f28/scenario_three_groups.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            DECISION,
            "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1.",
        )
        assertNextLine("[DEBUG] Restaurant Decision: Group 2 decided on restaurant 1.")
        assertNextLine("[DEBUG] Restaurant No Decision: Group 3 could not decide for a restaurant.")
    }
}

/** F29: a driver returns, keeps its id for the evening and then takes the next order. */
class F29TheDriverTakesASecondOrderAfterReturning : LogSkippingSystemTest() {
    override val name = "F29TheDriverTakesASecondOrderAfterReturning"
    override val description = "The driver delivers order 1, returns, and takes order 2 as driver 1 again."
    override val food = "f29/food_rice.json"
    override val restaurants = "f29/restaurants_one_driver.json"
    override val scenario = "f29/scenario_two_deliveries.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            PREPARATION,
            ORDER_1_TO_GROUP_1,
        )
        skipToAndAssert(
            "[INFO] Delivery Returned",
            "[INFO] Delivery Returned (R 1): Driver 1 has returned.",
        )
        skipToAndAssert(
            PREPARATION,
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 2 to group 2, " +
                "which will take 1 ticks.",
        )
    }
}

/** F30: no new customers in the last 3 ticks, so closing in tick 10 turns away tick 8. */
class F30NoNewCustomersInTheLastThreeTicks : LogSkippingSystemTest() {
    override val name = "F30NoNewCustomersInTheLastThreeTicks"
    override val description = "With closing in tick 10, tick 7 is served and tick 8 is turned away."
    override val food = F30_FOOD
    override val restaurants = "f30/restaurants_closing_at_ten.json"
    override val scenario = "f30/scenario_seven_and_eight.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            SEATING,
            GROUP_1_SEATED,
        )
        skipToAndAssert(NO_DECISION, GROUP_2_UNDECIDED)
    }
}

/** F30: only a delivery already given to a driver continues after the opening time. */
class F30ADeliveryOnTheRoadContinuesAfterClosing : LogSkippingSystemTest() {
    override val name = "F30ADeliveryOnTheRoadContinuesAfterClosing"
    override val description = "A delivery handed to a driver before closing still reaches the group after it."
    override val food = F30_FOOD
    override val restaurants = "f30/restaurants_closing_with_driver.json"
    override val scenario = "f30/scenario_delivery_arrives_after_closing.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToPrefix("[IMPORTANT] Simulation: Tick 11 ")
        skipToAndAssert(
            FINISHED,
            "[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.",
        )
    }
}

/** F30: customers are reset at the end of the evening, so the group can visit again. */
class F30CustomersAreResetForTheNextEvening : LogSkippingSystemTest() {
    override val name = "F30CustomersAreResetForTheNextEvening"
    override val description = "A group visiting two evenings is seated and served again on the second."
    override val food = F30_FOOD
    override val restaurants = "f30/restaurants_two_tables.json"
    override val scenario = "f30/scenario_group_on_two_evenings.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 50

    override suspend fun run() {
        skipToAndAssert(
            SEATING,
            GROUP_1_SEATED,
        )
        skipToPrefix("[IMPORTANT] Preparation: Preparation for evening 2 starts.")
        skipToAndAssert(
            SEATING,
            GROUP_1_SEATED,
        )
    }
}

/** F29: the waitstaff hands the meals over before the driver prepares to drive. */
class F29TheHandOverIsLoggedBeforeTheDriverPrepares : LogSkippingSystemTest() {
    override val name = "F29TheHandOverIsLoggedBeforeTheDriverPrepares"
    override val description = "The meals reach the driver before the driver prepares to drive."
    override val food = "f29/food_rice.json"
    override val restaurants = "f29/restaurants_one_driver.json"
    override val scenario = "f29/scenario_one_delivery.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Delivery",
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:2 meals to driver 1 for order 1.",
        )
        assertNextLine(
            ORDER_1_TO_GROUP_1,
        )
    }
}
