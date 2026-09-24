package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/shortstaffed"
private const val FOOD = "$DIR/food.json"

/**
 * ShortStaffed: an event of 20 uses up the SEATING load of both waiters exactly (10 each), so the
 * casual group arriving in the same tick finds no free waiter, tries again in tick 6 and is seated
 * then.
 */
class P03EventOfTwentyIsSeatedByBothWaitersAndBlocksTheNextGroup : SurvivorTest() {
    override val name = "P03EventOfTwentyIsSeatedByBothWaitersAndBlocksTheNextGroup"
    override val description = "An event of 20 fills both waiters (10 each); the casual group has to wait a tick."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_event_two_waiters.json"
    override val scenario = "$DIR/scenario_event_of_twenty.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, ORDERING, RATING),
            """
            4/5 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            4/5 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1,2.
            4/5 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:20 with waitstaff 1,2.
            4/5 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            4/5 FOH No Seating (R 1): No free waitstaff available for group 2.
            4/6 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.
            4/6 FOH Ordering (R 1): Group 2 placed order 2 of Chicken Rice:2 with waitstaff 1.
            4/7 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            4/8 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: REGULAR groups also try again one tick after finding no free waiter, at the table
 * reserved for them, and do not arrive a second time.
 */
class F16RegularRetriesAtItsReservedTable : SurvivorTest() {
    override val name = "F16RegularRetriesAtItsReservedTable"
    override val description =
        "A regular of 2 finds no waiter in tick 3 and is seated at its own table in tick 4."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_ten_ten_two.json"
    override val scenario = "$DIR/scenario_regular_retries.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, RATING),
            """
            1/3 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            1/3 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/3 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 2.
            1/4 FOH Seating (R 1): Group 2 seated at table 3 by waitstaff 1.
            1/5 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/6 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: a regular that finds no free waiter in its arrival tick and in the retry leaves with
 * a NEGATIVE rating, and that visit is a failed attempt. After two such evenings it does not come
 * back.
 */
class F22RegularStopsAfterTwoEveningsWithoutAFreeWaiter : SurvivorTest() {
    override val name = "F22RegularStopsAfterTwoEveningsWithoutAFreeWaiter"
    override val description = "Regular 3 finds no waiter twice on evenings 1 and 2, then skips evening 3."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_ten_ten_two.json"
    override val scenario = "$DIR/scenario_regular_two_strikes.json"
    override val maxTicks = 72
    override val group = 3

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, NO_RESERVING, RATING),
            """
            1/3 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            2/3 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            2/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            2/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            2/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 2 positive ratings and 2 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: in the retry tick the waiting group is handled in the usual order (type, then id),
 * so a new arrival with a lower id takes the only waiter's SEATING load and the retry fails for
 * good.
 */
class F16NewArrivalWithLowerIdGoesBeforeTheRetry : SurvivorTest() {
    override val name = "F16NewArrivalWithLowerIdGoesBeforeTheRetry"
    override val description = "Group 1 arriving in tick 4 is seated before the retry of group 3, which leaves."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_ten_ten_six.json"
    override val scenario = "$DIR/scenario_new_arrival_before_retry.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, RATING),
            """
            1/3 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            1/3 FOH Seating (R 1): Group 2 seated at table 1 by waitstaff 1.
            1/3 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            1/4 FOH Seating (R 1): Group 1 seated at table 2 by waitstaff 1.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            1/5 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 1 negative ratings.
            1/6 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: a waiter is free while tick load plus group size stays at or below 10, so 6 + 4 is
 * still seated by the same waiter and the third group has to try again next tick.
 */
class F17SixAndFourFillOneWaiterExactly : SurvivorTest() {
    override val name = "F17SixAndFourFillOneWaiterExactly"
    override val description = "Groups of 6 and 4 fill one waiter's SEATING load; group 3 waits until tick 3."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_six_four_two.json"
    override val scenario = "$DIR/scenario_six_plus_four.json"
    override val maxTicks = 6

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, ORDERING),
            """
            1/2 Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.
            1/2 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 FOH Ordering (R 1): Group 1 placed order 1 of Chicken Rice:6 with waitstaff 1.
            1/2 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            1/2 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.
            1/2 FOH Ordering (R 1): Group 2 placed order 2 of Chicken Rice:4 with waitstaff 1.
            1/2 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            1/2 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/3 FOH Seating (R 1): Group 3 seated at table 3 by waitstaff 1.
            1/3 FOH Ordering (R 1): Group 3 placed order 3 of Chicken Rice:2 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: with openingTickEnd 15 the last three ticks are 13 to 15, so a retry in tick 12 is
 * still accepted.
 */
class F16RetryJustBeforeTheLastThreeTicksIsSeated : SurvivorTest() {
    override val name = "F16RetryJustBeforeTheLastThreeTicksIsSeated"
    override val description = "Closing at 15: group 2 finds no waiter in tick 11 and is seated in tick 12."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_close_fifteen.json"
    override val scenario = "$DIR/scenario_retry_before_last_three.json"
    override val maxTicks = 24
    override val group = 2

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, RATING),
            """
            1/11 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            1/11 FOH No Seating (R 1): No free waitstaff available for group 2.
            1/12 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.
            1/14 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: with openingTickEnd 15, tick 13 is in the last three ticks, so the retry is refused
 * (forum 345) and the group leaves with a NEGATIVE rating in tick 13.
 */
class F16RetryInTheLastThreeTicksOfAnEarlyClosingIsRefused : SurvivorTest() {
    override val name = "F16RetryInTheLastThreeTicksOfAnEarlyClosingIsRefused"
    override val description = "Closing at 15: no waiter in tick 12, the retry in tick 13 is refused."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_close_fifteen.json"
    override val scenario = "$DIR/scenario_retry_in_last_three.json"
    override val maxTicks = 24
    override val group = 2

    override suspend fun run() {
        assertTrace(
            listOf(ARRIVAL, NO_SEATING, SEATING, RATING),
            """
            1/12 Restaurant Arrival (R 1): Group 2 arrived at restaurant 1.
            1/12 FOH No Seating (R 1): No free waitstaff available for group 2.
            1/13 Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: leaving without a seat is a negative experience; SOME rates it, NEVER does not.
 */
class P05LikelihoodsAfterTwoFailedSeatingAttempts : SurvivorTest() {
    override val name = "P05LikelihoodsAfterTwoFailedSeatingAttempts"
    override val description = "After two ticks without a waiter, SOME rates NEGATIVE and NEVER does not rate."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_ten_ten_two_two.json"
    override val scenario = "$DIR/scenario_likelihood_after_two_failures.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(NO_SEATING, RATING),
            """
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 4.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 4.
            1/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            1/5 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 1 negative ratings.
            1/6 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: after two ticks without a free waiter the group leaves and does *not* reconsider,
 * even when a second restaurant of the same type with four free waiters and a free table is open
 * next door. Exactly one Restaurant Decision is logged for the group, for restaurant 1; a mutant
 * that lets the group browse again picks restaurant 2 and produces a second decision line.
 */
class F25NoReconsiderationAfterTwoFailedSeatingAttempts : SurvivorTest() {
    override val name = "F25NoReconsiderationAfterTwoFailedSeatingAttempts"
    override val description =
        "With a free alternative restaurant open, the unseated group still leaves without deciding again."
    override val food = "$DIR/food_rice_only.json"
    override val restaurants = "$DIR/restaurants_busy_and_free_alternative.json"
    override val scenario = "$DIR/scenario_casual_two_strikes_with_alternative.json"
    override val maxTicks = 24
    override val group = 3

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ARRIVAL, SEATING, NO_SEATING, RATING),
            """
            1/3 Restaurant Decision: Group 3 decided on restaurant 1.
            1/3 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 5 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * ShortStaffed: the limit is exactly two attempts, not three. A third group of ten arrives in tick
 * 5 and would take the waiter's whole SEATING load again, but the small group is already gone after
 * its second failure in tick 4, so no third "No Seating" line is ever written for it. A mutant that
 * lets the group try once more keeps it waiting into tick 5.
 */
class F16TheSeatingRetryLimitIsTwoAttemptsNotThree : SurvivorTest() {
    override val name = "F16TheSeatingRetryLimitIsTwoAttemptsNotThree"
    override val description =
        "Blocked in ticks 3 and 4, the group leaves; the tick-5 blocker never produces a third refusal."
    override val food = "$DIR/food_rice_only.json"
    override val restaurants = "$DIR/restaurants_three_big_tables_one_waiter.json"
    override val scenario = "$DIR/scenario_blocked_three_ticks_running.json"
    override val maxTicks = 24
    override val group = 3

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, ARRIVAL, SEATING, NO_SEATING, RATING),
            """
            1/3 Restaurant Decision: Group 3 decided on restaurant 1.
            1/3 Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.
            1/3 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 FOH No Seating (R 1): No free waitstaff available for group 3.
            1/4 Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            """.trimIndent().lines(),
        )
    }
}
