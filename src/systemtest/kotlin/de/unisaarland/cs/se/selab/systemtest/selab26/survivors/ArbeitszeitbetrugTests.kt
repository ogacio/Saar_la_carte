package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/arbeitszeitbetrug"
private const val FOOD = "$DIR/food.json"

/**
 * Arbeitszeitbetrug: a driver whose group gave up still drives to it, fails in tick 11 and drives
 * the same 2 ticks back, returning in tick 13. The browsing service sees the driver as busy until
 * then, including in tick 13 itself, whose decisions come before the delivery step; group 4 deciding
 * in tick 14 gets the driver.
 */
class F29DriverIsBusyUntilTheTickAfterAFailedTripReturns : SurvivorTest() {
    override val name = "F29DriverIsBusyUntilTheTickAfterAFailedTripReturns"
    override val description =
        "After the failed delivery the driver is back in tick 13; groups deciding in 12 and 13 find no driver."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_one_driver.json"
    override val scenario = "$DIR/scenario_driver_back_after_a_failed_delivery.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ORDERING, SERVING, NO_SERVING, FOH_DELIVERY, DELIVERY, RATING),
            """
            1/1 Restaurant Decision: Group 1 decided on restaurant 1.
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:1,Slow Curry:1,Slow Stew:1.
            1/9 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:1,Slow Curry:1,Slow Stew:1 meals to driver 1 for order 1.
            1/9 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 2 ticks.
            1/9 Delivery Given Up (R 1): Group 1 gave up on waiting for delivery of order 1.
            1/9 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            1/10 Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.
            1/11 Delivery Driving (R 1): Driver 1 drove 10 km and needs 0 more ticks.
            1/11 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/11 Delivery Failed (R 1): Driver 1 failed to deliver order 1 to group 1.
            1/12 Restaurant No Decision: Group 2 could not decide for a restaurant.
            1/13 Restaurant No Decision: Group 3 could not decide for a restaurant.
            1/13 Delivery Returned (R 1): Driver 1 has returned.
            1/14 Restaurant Decision: Group 4 decided on restaurant 1.
            1/14 FOH Ordering (R 1): Group 4 placed order 2 of Slow Curry:1.
            1/17 FOH Delivery (R 1): Waitstaff 1 serves Slow Curry:1 meals to driver 1 for order 2.
            1/17 Delivery Preparation (R 1): Driver 1 prepares driving order 2 to group 4, which will take 1 ticks.
            1/18 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/18 Delivery Arrival (R 1): Driver 1 arrived at group 4 with order 2.
            1/18 Delivery Finished (R 1): Driver 1 gave delivery of order 2 to group 4.
            1/19 Delivery Returned (R 1): Driver 1 has returned.
            1/20 Delivery Finished Eating (R 1): Group 4 has finished eating.
            1/20 Rating (R 1): Group 4 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Arbeitszeitbetrug: handing meals to a driver is SERVING work, limited to 10 meals per waiter and
 * tick. Order 2 is handed over partly (driver 2 gets its id with the first meals) and only leaves in
 * tick 7, when its last 2 meals are handed over.
 */
class F20HandOverUsesTheWaitersServingLoad : SurvivorTest() {
    override val name = "F20HandOverUsesTheWaitersServingLoad"
    override val description =
        "One waiter hands 6 meals to driver 1 and 4 to driver 2 in tick 6, the last 2 in tick 7."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_one_waiter_two_drivers.json"
    override val scenario = "$DIR/scenario_two_deliveries_of_six.json"
    override val maxTicks = 14

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ORDERING, SERVING, NO_SERVING, FOH_DELIVERY, DELIVERY, RATING),
            """
            1/6 Restaurant Decision: Group 1 decided on restaurant 1.
            1/6 Restaurant Decision: Group 2 decided on restaurant 1.
            1/6 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:6.
            1/6 FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:6.
            1/6 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:6 meals to driver 1 for order 1.
            1/6 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:4 meals to driver 2 for order 2.
            1/6 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.
            1/7 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:2 meals to driver 2 for order 2.
            1/7 Delivery Preparation (R 1): Driver 2 prepares driving order 2 to group 2, which will take 1 ticks.
            1/7 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/7 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/7 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/8 Delivery Driving (R 1): Driver 2 drove 5 km and needs 0 more ticks.
            1/8 Delivery Arrival (R 1): Driver 2 arrived at group 2 with order 2.
            1/8 Delivery Finished (R 1): Driver 2 gave delivery of order 2 to group 2.
            1/8 Delivery Returned (R 1): Driver 1 has returned.
            1/9 Delivery Returned (R 1): Driver 2 has returned.
            1/9 Delivery Finished Eating (R 1): Group 1 has finished eating.
            1/9 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/10 Delivery Finished Eating (R 1): Group 2 has finished eating.
            1/10 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Arbeitszeitbetrug: in the serving step the waiter serves tables first and then loads drivers with
 * what is left of the 10 SERVING actions, so the delivery waits a tick for its sixth meal.
 */
class F20TablesAreServedBeforeTheDriverIsLoaded : SurvivorTest() {
    override val name = "F20TablesAreServedBeforeTheDriverIsLoaded"
    override val description =
        "The waiter serves the table of 5 first; the delivery of 6 gets 5 meals, its last one in tick 7."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_one_waiter_two_drivers.json"
    override val scenario = "$DIR/scenario_table_before_delivery.json"
    override val maxTicks = 14

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ORDERING, SERVING, NO_SERVING, FOH_DELIVERY, DELIVERY, RATING),
            """
            1/6 Restaurant Decision: Group 1 decided on restaurant 1.
            1/6 Restaurant Decision: Group 2 decided on restaurant 1.
            1/6 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:5 with waitstaff 1.
            1/6 FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:6.
            1/6 FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:5 to table 1 0 ticks after ordering.
            1/6 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:5 meals to driver 1 for order 2.
            1/7 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:1 meals to driver 1 for order 2.
            1/7 Delivery Preparation (R 1): Driver 1 prepares driving order 2 to group 2, which will take 1 ticks.
            1/8 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/8 Delivery Arrival (R 1): Driver 1 arrived at group 2 with order 2.
            1/8 Delivery Finished (R 1): Driver 1 gave delivery of order 2 to group 2.
            1/8 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/9 Delivery Returned (R 1): Driver 1 has returned.
            1/10 Delivery Finished Eating (R 1): Group 2 has finished eating.
            1/10 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Arbeitszeitbetrug: only deliveries already given to a driver continue after the opening time.
 * Driver 1 has its whole order and still delivers in tick 9; driver 2 has only 4 meals, the waiters
 * stop at closing, so group 2 gives up at visitingTick + 3 and rates NEGATIVE (forum 304).
 */
class F30NoHandOverAfterClosing : SurvivorTest() {
    override val name = "F30NoHandOverAfterClosing"
    override val description =
        "Closing at 8: driver 2 gets 4 of 6 meals in tick 8, nothing more; group 2 gives up in tick 12."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_closes_at_eight.json"
    override val scenario = "$DIR/scenario_partial_hand_over_at_closing.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ORDERING, SERVING, NO_SERVING, FOH_DELIVERY, DELIVERY, RATING),
            """
            1/5 Restaurant Decision: Group 1 decided on restaurant 1.
            1/5 Restaurant Decision: Group 2 decided on restaurant 1.
            1/5 FOH Ordering (R 1): Group 1 placed order 1 of Slow Stew:6.
            1/5 FOH Ordering (R 1): Group 2 placed order 2 of Slow Stew:6.
            1/8 FOH Delivery (R 1): Waitstaff 1 serves Slow Stew:6 meals to driver 1 for order 1.
            1/8 FOH Delivery (R 1): Waitstaff 1 serves Slow Stew:4 meals to driver 2 for order 2.
            1/8 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.
            1/9 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/9 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/9 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/10 Delivery Returned (R 1): Driver 1 has returned.
            1/11 Delivery Finished Eating (R 1): Group 1 has finished eating.
            1/11 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/12 Delivery Given Up (R 1): Group 2 gave up on waiting for delivery of order 2.
            1/12 Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, leading to 1 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Arbeitszeitbetrug: the driving log shows the cumulative distance of the current order only (forum
 * 367, staff), at most 5 km per tick, and the return trip takes as many ticks as the way out without
 * driving logs.
 */
class F29DrivingDistanceRestartsForEachOrder : SurvivorTest() {
    override val name = "F29DrivingDistanceRestartsForEachOrder"
    override val description = "The driver logs 5, 10, 13 km for the first order and 5, 8 km for the second."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_one_driver.json"
    override val scenario = "$DIR/scenario_thirteen_then_eight_km.json"
    override val maxTicks = 20

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, FOH_DELIVERY, DELIVERY, RATING),
            """
            1/4 Restaurant Decision: Group 1 decided on restaurant 1.
            1/4 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:1 meals to driver 1 for order 1.
            1/4 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 3 ticks.
            1/5 Delivery Driving (R 1): Driver 1 drove 5 km and needs 2 more ticks.
            1/6 Delivery Driving (R 1): Driver 1 drove 10 km and needs 1 more ticks.
            1/7 Delivery Driving (R 1): Driver 1 drove 13 km and needs 0 more ticks.
            1/7 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/7 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/9 Delivery Finished Eating (R 1): Group 1 has finished eating.
            1/9 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/10 Delivery Returned (R 1): Driver 1 has returned.
            1/12 Restaurant Decision: Group 2 decided on restaurant 1.
            1/12 FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:1 meals to driver 1 for order 2.
            1/12 Delivery Preparation (R 1): Driver 1 prepares driving order 2 to group 2, which will take 2 ticks.
            1/13 Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.
            1/14 Delivery Driving (R 1): Driver 1 drove 8 km and needs 0 more ticks.
            1/14 Delivery Arrival (R 1): Driver 1 arrived at group 2 with order 2.
            1/14 Delivery Finished (R 1): Driver 1 gave delivery of order 2 to group 2.
            1/16 Delivery Returned (R 1): Driver 1 has returned.
            1/16 Delivery Finished Eating (R 1): Group 2 has finished eating.
            1/16 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}
