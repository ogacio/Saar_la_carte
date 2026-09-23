package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/dinnerforone"

/**
 * DinnerForOne: every customer eats on their own clock. The quick dish is held back for two ticks,
 * then served alone in tick 3; the slow dish follows in tick 4. The members finish in ticks 5 and 6,
 * the eating status counts customers, and the group is escorted and rates once both are done.
 */
class F21MembersServedApartFinishEatingApart : SurvivorTest() {
    override val name = "F21MembersServedApartFinishEatingApart"
    override val description =
        "Rice Bowl is served in tick 3 and Slow Stew in 4; each member finishes 2 ticks later."
    override val food = "$DIR/food_quick_and_slow.json"
    override val restaurants = "$DIR/restaurants_two_cooks.json"
    override val scenario = "$DIR/scenario_pair_eats_apart.json"
    override val maxTicks = 7

    override suspend fun run() {
        assertTrace(
            listOf(
                ORDERING,
                NO_ORDERING,
                SERVING,
                NO_SERVING,
                NO_EATING,
                FINISHED_EATING,
                ESCORTING,
                RATING,
                EATING_STATUS,
                STATISTICS,
            ),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:1,Slow Stew:1 with waitstaff 1.
            1/1 FOH No Serving (R 1): Waitstaff 1 did not serve 1 meals to table 1.
            1/1 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/2 FOH No Serving (R 1): Waitstaff 1 did not serve 1 meals to table 1.
            1/2 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/3 FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:1 to table 1 2 ticks after ordering.
            1/3 FOH Eating Status (R 1): 1 customers are eating and 0 customers have finished eating this tick.
            1/4 FOH Serving (R 1): Waitstaff 1 serves Slow Stew:1 to table 1 3 ticks after ordering.
            1/4 FOH Eating Status (R 1): 2 customers are eating and 0 customers have finished eating this tick.
            1/5 FOH Finished Eating (R 1): 1 customers of group 1 have finished eating at table 1.
            1/5 FOH Eating Status (R 1): 1 customers are eating and 1 customers have finished eating this tick.
            1/6 FOH Finished Eating (R 1): 1 customers of group 1 have finished eating at table 1.
            1/6 FOH Eating Status (R 1): 0 customers are eating and 1 customers have finished eating this tick.
            1/6 FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 1 from table 1 outside.
            1/6 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/7 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/7 Simulation Statistics: Restaurant 1 cooked 2 meals.
            1/7 Simulation Statistics: Restaurant 1 served 2 customers.
            1/7 Simulation Statistics: Restaurant 1 delivered meals to 0 customers.
            1/7 Simulation Statistics: Restaurant 1 received 1 ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * DinnerForOne: SERVING load is counted per meal. After six meals for group 1 the only waiter has 4
 * SERVING actions left, not enough for group 2's five, so that table waits for tick 3.
 */
class F19ServingLoadCountsMealsNotTables : SurvivorTest() {
    override val name = "F19ServingLoadCountsMealsNotTables"
    override val description =
        "Six meals for group 1 and five for group 2 are ready in tick 2; the waiter serves six, five wait."
    override val food = "$DIR/food_medium_and_quick.json"
    override val restaurants = "$DIR/restaurants_two_six_tables.json"
    override val scenario = "$DIR/scenario_eleven_meals_ready.json"
    override val maxTicks = 6

    override suspend fun run() {
        assertTrace(
            listOf(
                ORDERING,
                NO_ORDERING,
                SERVING,
                NO_SERVING,
                NO_EATING,
                FINISHED_EATING,
                ESCORTING,
                RATING,
                STATISTICS,
            ),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:6 with waitstaff 1.
            1/2 FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:5 with waitstaff 1.
            1/2 FOH Serving (R 1): Waitstaff 1 serves Egg Rice:6 to table 1 1 ticks after ordering.
            1/2 FOH No Serving (R 1): Waitstaff 1 did not serve 5 meals to table 2.
            1/3 FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:5 to table 2 1 ticks after ordering.
            1/4 FOH Finished Eating (R 1): 6 customers of group 1 have finished eating at table 1.
            1/4 FOH Escorting (R 1): Waitstaff 1 escorts 6 customers of group 1 from table 1 outside.
            1/4 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/5 FOH Finished Eating (R 1): 5 customers of group 2 have finished eating at table 2.
            1/5 FOH Escorting (R 1): Waitstaff 1 escorts 5 customers of group 2 from table 2 outside.
            1/5 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            1/6 Simulation Statistics: Restaurant 1 cooked 11 meals.
            1/6 Simulation Statistics: Restaurant 1 served 11 customers.
            1/6 Simulation Statistics: Restaurant 1 delivered meals to 0 customers.
            1/6 Simulation Statistics: Restaurant 1 received 2 ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * DinnerForOne: waiting and leaving are per customer. One cook makes First Bowl (ticks 1-3), Middle
 * Bowl (4) and Last Bowl (5-8); three customers are served in tick 5 and finish in 7, the two
 * waiting for Last Bowl leave in tick 7 when the two extra ticks are over.
 */
class F27TwoUnservedMembersLeaveTogether : SurvivorTest() {
    override val name = "F27TwoUnservedMembersLeaveTogether"
    override val description =
        "Of a group of 5, three are served in tick 5; the two Last Bowl customers leave in tick 7."
    override val food = "$DIR/food_first_middle_last.json"
    override val restaurants = "$DIR/restaurants_one_cook.json"
    override val scenario = "$DIR/scenario_two_leave_unserved.json"
    override val maxTicks = 9

    override suspend fun run() {
        assertTrace(
            listOf(
                ORDERING,
                NO_ORDERING,
                SERVING,
                NO_SERVING,
                NO_EATING,
                FINISHED_EATING,
                ESCORTING,
                RATING,
                EATING_STATUS,
            ),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of First Bowl:2,Last Bowl:2,Middle Bowl:1 with waitstaff 1.
            1/1 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/2 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/3 FOH No Serving (R 1): Waitstaff 1 did not serve 2 meals to table 1.
            1/3 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/4 FOH No Serving (R 1): Waitstaff 1 did not serve 3 meals to table 1.
            1/4 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/5 FOH Serving (R 1): Waitstaff 1 serves First Bowl:2,Middle Bowl:1 to table 1 4 ticks after ordering.
            1/5 FOH Eating Status (R 1): 3 customers are eating and 0 customers have finished eating this tick.
            1/6 FOH Eating Status (R 1): 3 customers are eating and 0 customers have finished eating this tick.
            1/7 Restaurant No Eating (R 1): 2 customers of group 1 leave table 1 due to not being served.
            1/7 FOH Finished Eating (R 1): 3 customers of group 1 have finished eating at table 1.
            1/7 FOH Eating Status (R 1): 0 customers are eating and 3 customers have finished eating this tick.
            1/7 FOH Escorting (R 1): Waitstaff 1 escorts 3 customers of group 1 from table 1 outside.
            1/7 Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, leading to 0 positive ratings and 1 negative ratings.
            1/8 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/9 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            """.trimIndent().lines(),
        )
    }
}

/**
 * DinnerForOne: event members choose one by one. The favourite of the event beats a member's own
 * favourite dish, but not an excluded ingredient, so 4 members order Rice Bowl and the 2 excluding
 * rice order Egg Plate.
 */
class P03EventMembersOrderOneByOne : SurvivorTest() {
    override val name = "P03EventMembersOrderOneByOne"
    override val description =
        "An event of 6 orders the favourite for 4 members and Egg Plate for the 2 excluding rice."
    override val food = "$DIR/food_event.json"
    override val restaurants = "$DIR/restaurants_event.json"
    override val scenario = "$DIR/scenario_event_members_order_alone.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertTrace(
            listOf(
                ORDERING,
                NO_ORDERING,
                SERVING,
                NO_SERVING,
                NO_EATING,
                FINISHED_EATING,
                ESCORTING,
                RATING,
                STATISTICS,
            ),
            """
            4/2 FOH Ordering (R 1): Group 1 placed order 1 of Egg Plate:2,Rice Bowl:4 with waitstaff 1.
            4/2 FOH No Serving (R 1): Waitstaff 1 did not serve 4 meals to table 1.
            4/3 FOH Serving (R 1): Waitstaff 1 serves Egg Plate:2,Rice Bowl:4 to table 1 1 ticks after ordering.
            4/5 FOH Finished Eating (R 1): 6 customers of group 1 have finished eating at table 1.
            4/5 FOH Escorting (R 1): Waitstaff 1 escorts 6 customers of group 1 from table 1 outside.
            4/5 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            4/24 Simulation Statistics: Restaurant 1 cooked 6 meals.
            4/24 Simulation Statistics: Restaurant 1 served 6 customers.
            4/24 Simulation Statistics: Restaurant 1 delivered meals to 0 customers.
            4/24 Simulation Statistics: Restaurant 1 received 1 ratings.
            """.trimIndent().lines(),
        )
    }
}

/**
 * DinnerForOne: ESCORTING load is counted per customer and can be done partially. Group 1's last
 * member finishes in the same tick as group 2, so 12 customers wait for one waiter: 8 of group 1 and
 * 2 of group 2 leave in tick 6, the other 2 in tick 7, when group 2 rates.
 */
class F21EscortingLoadCountsCustomers : SurvivorTest() {
    override val name = "F21EscortingLoadCountsCustomers"
    override val description =
        "Groups of 8 and 4 finish together; the waiter escorts 8 and 2, the last 2 in the next tick."
    override val food = "$DIR/food_quick_and_slow.json"
    override val restaurants = "$DIR/restaurants_two_cooks.json"
    override val scenario = "$DIR/scenario_twelve_to_escort.json"
    override val maxTicks = 8

    override suspend fun run() {
        assertTrace(
            listOf(
                ORDERING,
                NO_ORDERING,
                SERVING,
                NO_SERVING,
                NO_EATING,
                FINISHED_EATING,
                ESCORTING,
                RATING,
                EATING_STATUS,
                STATISTICS,
            ),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:7,Slow Stew:1 with waitstaff 1.
            1/1 FOH No Serving (R 1): Waitstaff 1 did not serve 7 meals to table 2.
            1/1 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/2 FOH No Serving (R 1): Waitstaff 1 did not serve 7 meals to table 2.
            1/2 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/3 FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:7 to table 2 2 ticks after ordering.
            1/3 FOH Eating Status (R 1): 7 customers are eating and 0 customers have finished eating this tick.
            1/4 FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:4 with waitstaff 1.
            1/4 FOH Serving (R 1): Waitstaff 1 serves Slow Stew:1 to table 2 3 ticks after ordering.
            1/4 FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:4 to table 3 0 ticks after ordering.
            1/4 FOH Eating Status (R 1): 12 customers are eating and 0 customers have finished eating this tick.
            1/5 FOH Finished Eating (R 1): 7 customers of group 1 have finished eating at table 2.
            1/5 FOH Eating Status (R 1): 5 customers are eating and 7 customers have finished eating this tick.
            1/6 FOH Finished Eating (R 1): 1 customers of group 1 have finished eating at table 2.
            1/6 FOH Finished Eating (R 1): 4 customers of group 2 have finished eating at table 3.
            1/6 FOH Eating Status (R 1): 0 customers are eating and 5 customers have finished eating this tick.
            1/6 FOH Escorting (R 1): Waitstaff 1 escorts 8 customers of group 1 from table 2 outside.
            1/6 FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 2 from table 3 outside.
            1/6 Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, leading to 1 positive ratings and 0 negative ratings.
            1/7 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/7 FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 2 from table 3 outside.
            1/7 Rating (R 1): Group 2 rates the restaurant 1 with POSITIVE rating, leading to 2 positive ratings and 0 negative ratings.
            1/8 FOH Eating Status (R 1): 0 customers are eating and 0 customers have finished eating this tick.
            1/8 Simulation Statistics: Restaurant 1 cooked 12 meals.
            1/8 Simulation Statistics: Restaurant 1 served 12 customers.
            1/8 Simulation Statistics: Restaurant 1 delivered meals to 0 customers.
            1/8 Simulation Statistics: Restaurant 1 received 2 ratings.
            """.trimIndent().lines(),
        )
    }
}
