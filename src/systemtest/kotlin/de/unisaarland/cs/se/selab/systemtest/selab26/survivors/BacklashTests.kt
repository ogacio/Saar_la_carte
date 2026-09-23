package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/backlash"
private const val FOOD = "$DIR/food.json"
private const val FOOD_DELIVERY = "$DIR/food_delivery.json"
private const val RESTAURANTS_ONE_DRIVER = "$DIR/restaurants_one_driver.json"

/**
 * Backlash: a failed reservation is rated in the tick of the restaurant's openingTickStart, not in
 * tick 1 (specification adjustment of 23 Sep, forum 363), and the counts include the 10 positive and
 * 5 negative ratings from the JSON. After two failed reservations regular 2 does not come back on
 * evening 3.
 */
class P05FailedRegularReservationRatesInTheOpeningTick : SurvivorTest() {
    override val name = "P05FailedRegularReservationRatesInTheOpeningTick"
    override val description = "The regular without a table rates NEGATIVE in tick 3, when the restaurant opens."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_opens_at_three.json"
    override val scenario = "$DIR/scenario_regular_without_table.json"
    override val maxTicks = 72

    override suspend fun run() {
        assertTrace(
            listOf(NO_RESERVING, ARRIVAL, SEATING, RATING),
            """
            before FOH No Reserving (R 1): No table could be reserved for group 2.
            1/3 Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, leading to 10 positive ratings and 6 negative ratings.
            1/5 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            1/5 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/7 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 11 positive ratings and 6 negative ratings.
            1/24 FOH No Reserving (R 1): No table could be reserved for group 2.
            2/3 Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, leading to 11 positive ratings and 7 negative ratings.
            2/5 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            2/5 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            2/7 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 12 positive ratings and 7 negative ratings.
            3/5 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            3/5 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            3/7 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 13 positive ratings and 7 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: two events of 4 book the restaurant on evening 1 for evening 4; only the first gets the
 * 6-seat table. The second rates NEGATIVE in the restaurant's opening tick 5 of evening 4, counted
 * on top of the 2 positive ratings from the JSON.
 */
class P05FailedEventReservationRatesInTheOpeningTick : SurvivorTest() {
    override val name = "P05FailedEventReservationRatesInTheOpeningTick"
    override val description =
        "The event without a table rates NEGATIVE in tick 5 of evening 4, when the restaurant opens."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_event_opens_at_five.json"
    override val scenario = "$DIR/scenario_second_event_without_table.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_RESERVING, ARRIVAL, SEATING, RATING),
            """
            1/1 Restaurant Decision: Group 1 decided on restaurant 1.
            1/1 Restaurant Decision: Group 2 decided on restaurant 1.
            3/24 FOH No Reserving (R 1): No table could be reserved for group 2.
            4/5 Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, leading to 2 positive ratings and 1 negative ratings.
            4/6 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            4/6 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            4/8 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 3 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: ratings made during the simulation change later restaurant choices. Restaurant 1 starts
 * ahead (1 positive rating from the JSON), groups 3 and 4 find no waiter twice and rate it NEGATIVE,
 * so in tick 6 restaurant 2 has the higher difference (0 against -1).
 */
class F28NegativeRatingsSendTheNextGroupElsewhere : SurvivorTest() {
    override val name = "F28NegativeRatingsSendTheNextGroupElsewhere"
    override val description = "Two NEGATIVE ratings in tick 3 make group 5 choose restaurant 2 in tick 6."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_two_asian.json"
    override val scenario = "$DIR/scenario_negative_ratings_redirect.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_SEATING, SEATING, RATING),
            """
            1/2 Restaurant Decision: Group 1 decided on restaurant 1.
            1/2 Restaurant Decision: Group 3 decided on restaurant 1.
            1/2 Restaurant Decision: Group 4 decided on restaurant 1.
            1/2 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/2 FOH No Seating (R 1): No free waitstaff available for group 4.
            1/3 Restaurant Decision: Group 2 decided on restaurant 1.
            1/3 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 4.
            1/3 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 1 positive ratings and 1 negative ratings.
            1/3 Rating (R 1): Group 4 rates the restaurant 1 with NEGATIVE rating, leading to 1 positive ratings and 2 negative ratings.
            1/6 Restaurant Decision: Group 5 decided on restaurant 2.
            1/6 FOH Seating (R 2): Group 5 seated at table 2 by waitstaff 1.
            1/8 Rating (R 2): Group 5 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: food that arrives not at all for one customer makes the whole group's experience
 * negative, so even a SOME group rates, once, in the tick its last member leaves; the counts include
 * the 4 and 4 ratings from the JSON.
 */
class P05GroupWithAnUnservedMemberRatesNegativeOnce : SurvivorTest() {
    override val name = "P05GroupWithAnUnservedMemberRatesNegativeOnce"
    override val description =
        "Two members eat, the third leaves unserved in tick 7: one NEGATIVE rating from SOME."
    override val food = "$DIR/food_mixed_waiting.json"
    override val restaurants = "$DIR/restaurants_mixed_waiting.json"
    override val scenario = "$DIR/scenario_mixed_waiting_some.json"
    override val maxTicks = 10

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, SERVING, NO_EATING, FINISHED_EATING, RATING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of First Bowl:1,Last Bowl:1,Middle Bowl:1 with waitstaff 1.
            1/5 FOH Serving (R 1): Waitstaff 1 serves First Bowl:1,Middle Bowl:1 to table 1 4 ticks after ordering.
            1/7 Restaurant No Eating (R 1): 1 customers of group 1 leave table 1 due to not being served.
            1/7 FOH Finished Eating (R 1): 2 customers of group 1 have finished eating at table 1.
            1/7 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, leading to 4 positive ratings and 5 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: a delivery the group gave up on is a negative experience, rated once in the give-up tick
 * even by a SOME group. The driver still drives on, fails on arrival and returns without a second
 * rating.
 */
class P05GivenUpDeliveryRatesOnceWithSome : SurvivorTest() {
    override val name = "P05GivenUpDeliveryRatesOnceWithSome"
    override val description =
        "The group gives up in tick 13 and rates NEGATIVE; the driver fails in 15 and returns in 20."
    override val food = FOOD_DELIVERY
    override val restaurants = RESTAURANTS_ONE_DRIVER
    override val scenario = "$DIR/scenario_delivery_given_up_some.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DELIVERY, RATING),
            """
            1/10 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 5 ticks.
            1/11 Delivery Driving (R 1): Driver 1 drove 5 km and needs 4 more ticks.
            1/12 Delivery Driving (R 1): Driver 1 drove 10 km and needs 3 more ticks.
            1/13 Delivery Driving (R 1): Driver 1 drove 15 km and needs 2 more ticks.
            1/13 Delivery Given Up (R 1): Group 1 gave up on waiting for delivery of order 1.
            1/13 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, leading to 3 positive ratings and 2 negative ratings.
            1/14 Delivery Driving (R 1): Driver 1 drove 20 km and needs 1 more ticks.
            1/15 Delivery Driving (R 1): Driver 1 drove 25 km and needs 0 more ticks.
            1/15 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/15 Delivery Failed (R 1): Driver 1 failed to deliver order 1 to group 1.
            1/20 Delivery Returned (R 1): Driver 1 has returned.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: a delivery is neutral only when it arrives at the visitingTick. Arriving one tick later
 * is too late, so the ALWAYS group rates NEGATIVE after eating.
 */
class P05LateDeliveryRatesNegative : SurvivorTest() {
    override val name = "P05LateDeliveryRatesNegative"
    override val description =
        "The delivery arrives in tick 11, one tick after visitingTick 10: NEGATIVE after eating."
    override val food = FOOD_DELIVERY
    override val restaurants = RESTAURANTS_ONE_DRIVER
    override val scenario = "$DIR/scenario_delivery_one_tick_late.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DELIVERY, RATING),
            """
            1/10 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.
            1/11 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/11 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/11 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/12 Delivery Returned (R 1): Driver 1 has returned.
            1/13 Delivery Finished Eating (R 1): Group 1 has finished eating.
            1/13 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, leading to 3 positive ratings and 2 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: a delivery arriving exactly at the visitingTick is a neutral experience, which SOME
 * groups do not rate.
 */
class P05OnTimeDeliveryIsNeutralForSome : SurvivorTest() {
    override val name = "P05OnTimeDeliveryIsNeutralForSome"
    override val description = "The delivery arrives exactly in visitingTick 10; the SOME group does not rate."
    override val food = FOOD_DELIVERY
    override val restaurants = RESTAURANTS_ONE_DRIVER
    override val scenario = "$DIR/scenario_delivery_on_time_some.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DELIVERY, RATING),
            """
            1/9 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.
            1/10 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/10 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/10 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/11 Delivery Returned (R 1): Driver 1 has returned.
            1/12 Delivery Finished Eating (R 1): Group 1 has finished eating.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: a neutral delivery experience still gets a POSITIVE rating from an ALWAYS group, never a
 * NEGATIVE one.
 */
class P05OnTimeDeliveryIsPositiveForAlways : SurvivorTest() {
    override val name = "P05OnTimeDeliveryIsPositiveForAlways"
    override val description = "The delivery arrives exactly in visitingTick 10; the ALWAYS group rates POSITIVE."
    override val food = FOOD_DELIVERY
    override val restaurants = RESTAURANTS_ONE_DRIVER
    override val scenario = "$DIR/scenario_delivery_on_time_always.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertTrace(
            listOf(DELIVERY, RATING),
            """
            1/9 Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.
            1/10 Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.
            1/10 Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.
            1/10 Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.
            1/11 Delivery Returned (R 1): Driver 1 has returned.
            1/12 Delivery Finished Eating (R 1): Group 1 has finished eating.
            1/12 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 4 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * Backlash: every regular creates a rating for every visit, and the counts in the log start from the
 * ratings in the restaurant JSON.
 */
class P05RegularRatesEveryVisitOnTopOfTheSeeds : SurvivorTest() {
    override val name = "P05RegularRatesEveryVisitOnTopOfTheSeeds"
    override val description =
        "A regular rates POSITIVE on both evenings, counted on top of 3 and 3 JSON ratings."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_regular_home.json"
    override val scenario = "$DIR/scenario_regular_two_visits.json"
    override val maxTicks = 48

    override suspend fun run() {
        assertTrace(
            listOf(RATING),
            """
            1/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 4 positive ratings and 3 negative ratings.
            2/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 5 positive ratings and 3 negative ratings.
            """.trimIndent().lines(),
        )
    }
}
