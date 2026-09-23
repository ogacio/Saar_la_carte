package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/freeforall"
private const val FOOD = "$DIR/food.json"

/**
 * FreeForAll: the browsing service counts available seats per table type, so a group asking for a
 * BAR table is only offered restaurant 2, while the COMMON group takes restaurant 1 (lowest id).
 */
class F28BrowsingCountsSeatsPerTableType : SurvivorTest() {
    override val name = "F28BrowsingCountsSeatsPerTableType"
    override val description =
        "The BAR pair goes to restaurant 2, the only one with BAR seats; the COMMON pair to 1."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_common_and_bar.json"
    override val scenario = "$DIR/scenario_bar_and_common_group.json"
    override val maxTicks = 4

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/2 Restaurant Decision: Group 1 decided on restaurant 1.
            1/2 Restaurant Decision: Group 2 decided on restaurant 2.
            1/2 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 FOH Seating (R 2): Group 2 seated at table 1 by waitstaff 1.
            1/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/4 Rating (R 2): Group 2 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * FreeForAll: seats on a table reserved for a REGULAR group are not available, neither before the
 * regular arrives (tick 2) nor after it has left (tick 15), so both casual groups are sent to
 * restaurant 2.
 */
class F28AReservedTableIsNotAFreeSeat : SurvivorTest() {
    override val name = "F28AReservedTableIsNotAFreeSeat"
    override val description =
        "Restaurant 1's only table is reserved for a regular, so casual groups go to restaurant 2."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_four_and_four.json"
    override val scenario = "$DIR/scenario_reserved_table_is_not_free.json"
    override val maxTicks = 18

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/2 Restaurant Decision: Group 2 decided on restaurant 2.
            1/2 FOH Seating (R 2): Group 2 seated at table 1 by waitstaff 1.
            1/4 Rating (R 2): Group 2 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/10 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/12 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/15 Restaurant Decision: Group 3 decided on restaurant 2.
            1/15 FOH Seating (R 2): Group 3 seated at table 1 by waitstaff 1.
            1/17 Rating (R 2): Group 3 rates the restaurant 2 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * FreeForAll: each decision in the tick reduces the estimated seats of the wanted table type by the
 * group size. Restaurant 1 still has 10 COMMON seats, but its 4 BAR seats are gone after two BAR
 * pairs.
 */
class F28BarSeatsShrinkAsBarGroupsDecide : SurvivorTest() {
    override val name = "F28BarSeatsShrinkAsBarGroupsDecide"
    override val description =
        "Two BAR pairs fill restaurant 1's 4 BAR seats; the third pair goes to restaurant 2."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_bar_seats.json"
    override val scenario = "$DIR/scenario_three_bar_pairs.json"
    override val maxTicks = 4

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/2 Restaurant Decision: Group 1 decided on restaurant 1.
            1/2 Restaurant Decision: Group 2 decided on restaurant 1.
            1/2 Restaurant Decision: Group 3 decided on restaurant 2.
            1/2 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.
            1/2 FOH Seating (R 2): Group 3 seated at table 1 by waitstaff 1.
            1/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/4 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            1/4 Rating (R 2): Group 3 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * FreeForAll: the browsing service sees which tables are occupied. While group 1 eats at restaurant
 * 1's only table, group 2 is sent to restaurant 2; after group 1 has left in tick 3, restaurant 1 is
 * offered again.
 */
class F28AnOccupiedTableIsNotOfferedUntilItIsFree : SurvivorTest() {
    override val name = "F28AnOccupiedTableIsNotOfferedUntilItIsFree"
    override val description = "Group 2 finds restaurant 1 occupied in tick 2; in tick 6 group 3 gets it again."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_four_and_four.json"
    override val scenario = "$DIR/scenario_occupied_then_free.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/1 Restaurant Decision: Group 1 decided on restaurant 1.
            1/1 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 Restaurant Decision: Group 2 decided on restaurant 2.
            1/2 FOH Seating (R 2): Group 2 seated at table 1 by waitstaff 1.
            1/3 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/4 Rating (R 2): Group 2 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/6 Restaurant Decision: Group 3 decided on restaurant 1.
            1/6 FOH Seating (R 1): Group 3 seated at table 1 by waitstaff 1.
            1/8 Rating (R 1): Group 3 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * FreeForAll: for events the browsing service subtracts the sizes of prior EVENT reservations for
 * the same evening. Event 2 (4 people) no longer fits into restaurant 1 on evening 4, but event 3 on
 * evening 5 does.
 */
class P04EventSeatsCountPriorEventReservations : SurvivorTest() {
    override val name = "P04EventSeatsCountPriorEventReservations"
    override val description =
        "8 of 10 event seats are booked for evening 4, so the second event goes to restaurant 2."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_event_ten_and_four.json"
    override val scenario = "$DIR/scenario_event_capacity.json"
    override val maxTicks = 120

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/1 Restaurant Decision: Group 1 decided on restaurant 1.
            1/1 Restaurant Decision: Group 2 decided on restaurant 2.
            2/1 Restaurant Decision: Group 3 decided on restaurant 1.
            4/5 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            4/5 FOH Seating (R 2): Group 2 seated at table 1 by waitstaff 1.
            4/7 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            4/7 Rating (R 2): Group 2 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            5/5 FOH Seating (R 1): Group 3 seated at table 1 by waitstaff 1.
            5/7 Rating (R 1): Group 3 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * FreeForAll: a decision reduces the estimated seats by the size of the group, not by one table or
 * one seat: after group 1 only 2 of restaurant 1's 6 seats remain.
 */
class F28SeatsShrinkByTheGroupSize : SurvivorTest() {
    override val name = "F28SeatsShrinkByTheGroupSize"
    override val description =
        "Two groups of 4 in one tick: 6 seats hold only the first, the second goes to restaurant 2."
    override val food = FOOD
    override val restaurants = "$DIR/restaurants_six_seats_and_four.json"
    override val scenario = "$DIR/scenario_two_fours.json"
    override val maxTicks = 4

    override suspend fun run() {
        assertTrace(
            listOf(DECISION, NO_DECISION, NO_RESERVING, SEATING, NO_SEATING, MERGING, RATING),
            """
            1/2 Restaurant Decision: Group 1 decided on restaurant 1.
            1/2 Restaurant Decision: Group 2 decided on restaurant 2.
            1/2 FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.
            1/2 FOH Seating (R 2): Group 2 seated at table 1 by waitstaff 1.
            1/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/4 Rating (R 2): Group 2 rates the restaurant 2 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            """.trimIndent().lines(),
        )
    }
}
