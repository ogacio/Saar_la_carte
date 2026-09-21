package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val ONE_SMALL_TABLE = "foh/f14/restaurants_one_small_table.json"
private const val BAR_ONLY = "foh/f16/restaurants_bar_only.json"
private const val IMPORTANT = "IMPORTANT"
private const val ORDERING_PREFIX = "[IMPORTANT] FOH Ordering"
private const val ORDER_RICE_BOWL_FOR_FOUR =
    "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:4 with waitstaff 1."
private const val SEATING_PREFIX = "[IMPORTANT] FOH Seating"
private const val GROUP_1_SEATED = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."
private const val INFO = "INFO"
private const val ARRIVAL_PREFIX = "[INFO] Restaurant Arrival"
private const val GROUP_1_ARRIVED = "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1."
private const val FOOD_RICE = "foh/food_rice.json"
private const val FOOD_MENU = "foh/food_menu.json"
private const val DEBUG = "DEBUG"

/** F14: with one table, the regular group with the higher id gets no reservation. */
class F14RegularWithoutTableIsNotReserved : LogSkippingSystemTest() {
    override val name = "F14RegularWithoutTableIsNotReserved"
    override val description = "Two regular groups of 2 and one 2-seat table: group 2 is not reserved."
    override val food = FOOD_RICE
    override val restaurants = ONE_SMALL_TABLE
    override val scenario = "foh/f14/scenario_two_regulars.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 1

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] Preparation", "[IMPORTANT] Preparation: Preparation for evening 1 starts.")
        assertNextLine("[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 2.")
        assertNextLine("[IMPORTANT] Serving: Serving of evening 1 starts.")
    }
}

/** F14: EVENT groups are reserved before REGULAR groups, even with a higher id. */
class F14EventReservedBeforeRegular : LogSkippingSystemTest() {
    override val name = "F14EventReservedBeforeRegular"
    override val description = "Event group 2 takes the only table on evening 4, regular group 1 is not reserved."
    override val food = FOOD_RICE
    override val restaurants = "foh/f14/restaurants_event_host.json"
    override val scenario = "foh/f14/scenario_event_and_regular.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 73

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 4",
            "[IMPORTANT] Preparation: Preparation for evening 4 starts.",
        )
        assertNextLine("[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 1.")
        assertNextLine("[IMPORTANT] Serving: Serving of evening 4 starts.")
    }
}

/** F16: a casual group of four is seated at two merged 2-seat tables and orders right away. */
class F16CasualGroupSeatedAtMergedTable : LogSkippingSystemTest() {
    override val name = "F16CasualGroupSeatedAtMergedTable"
    override val description = "Arrival, merge of tables 1 and 2, seating by waitstaff 1 and the first order."
    override val food = FOOD_RICE
    override val restaurants = "foh/f16/restaurants_two_small_tables.json"
    override val scenario = "foh/f16/scenario_group_of_four.json"
    override val logLevel = INFO
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ARRIVAL_PREFIX,
            GROUP_1_ARRIVED,
        )
        assertNextLine("[INFO] FOH Merging Tables (R 1): For group 1 the tables 1,2 were merged into 1.")
        assertNextLine(GROUP_1_SEATED)
        assertNextLine(ORDER_RICE_BOWL_FOR_FOUR)
    }
}

/** F16: the only waiter reaches the SEATING limit with a group of ten; the next group waits one tick. */
class F16NoFreeWaiterThenSeatedNextTick : LogSkippingSystemTest() {
    override val name = "F16NoFreeWaiterThenSeatedNextTick"
    override val description = "Group 2 finds no free waiter in tick 3 and is seated by waitstaff 1 in tick 4."
    override val food = FOOD_RICE
    override val restaurants = "foh/f16/restaurants_ten_and_two.json"
    override val scenario = "foh/f16/scenario_ten_then_two.json"
    override val logLevel = INFO
    override val maxTicks = 4

    override suspend fun run() {
        skipToAndAssert(
            SEATING_PREFIX,
            GROUP_1_SEATED,
        )
        skipToAndAssert(
            "[INFO] FOH No Seating",
            "[INFO] FOH No Seating (R 1): No free waitstaff available for group 2.",
        )
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 4",
            "[IMPORTANT] Simulation: Tick 4 (1) started.",
        )
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.",
        )
    }
}

/** F16 + P05: BAR tables cannot be merged, so the group is sent away and rates negatively. */
class F16BarGroupSentAwayRatesNegative : LogSkippingSystemTest() {
    override val name = "F16BarGroupSentAwayRatesNegative"
    override val description = "A group of 3 wants two 2-seat BAR tables: sent away, NEGATIVE rating, no service."
    override val food = FOOD_RICE
    override val restaurants = BAR_ONLY
    override val scenario = "foh/f16/scenario_bar_group_of_three.json"
    override val logLevel = INFO
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ARRIVAL_PREFIX,
            GROUP_1_ARRIVED,
        )
        assertNextLine(
            "[INFO] FOH No Seating (R 1): Assigned waitstaff 1 but no table available, group 1 is sent away.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
        assertStatistics(1, cooked = 0, served = 0, delivered = 0, ratings = 1)
    }
}

/** P05: a casual group with rating likelihood NEVER leaves no rating even after a negative experience. */
class P05NeverLikelihoodLeavesNoRating : LogSkippingSystemTest() {
    override val name = "P05NeverLikelihoodLeavesNoRating"
    override val description = "The sent-away BAR group with likelihood NEVER does not rate."
    override val food = FOOD_RICE
    override val restaurants = BAR_ONLY
    override val scenario = "foh/p05/scenario_bar_group_never_rates.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        assertStatistics(1, cooked = 0, served = 0, delivered = 0, ratings = 0)
    }
}

/** F18: one customer orders its favourite dish, the other one without preferences the highest recipe id. */
class F18FavouriteDishAndHighestRecipeId : LogSkippingSystemTest() {
    override val name = "F18FavouriteDishAndHighestRecipeId"
    override val description = "Order 1 contains Rice Bowl (favourite) and Tofu Rice (highest id)."
    override val food = FOOD_MENU
    override val restaurants = "foh/f18/restaurants_two_dishes.json"
    override val scenario = "foh/f18/scenario_favourite_and_default.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:1,Tofu Rice:1 with waitstaff 1.",
        )
    }
}

/**
 * F13: a dish no cook of the restaurant can cook is not on the menu. The group has two customers,
 * because a single customer fills only half of the smallest possible table (two seats) and would be
 * sent away by the three quarter rule before ordering.
 */
class F13DishWithoutEligibleCookIsNotOrdered : LogSkippingSystemTest() {
    override val name = "F13DishWithoutEligibleCookIsNotOrdered"
    override val description = "Tofu Salad (id 3, VEGETABLE only) is skipped; both customers order Rice Bowl."
    override val food = FOOD_MENU
    override val restaurants = "foh/f13/restaurants_no_vegetable_cook.json"
    override val scenario = "foh/f13/scenario_group_of_two.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:2 with waitstaff 1.",
        )
    }
}

/** F19, F21, P05, F07: one casual visit from serving to rating and the statistics. */
class F21VisitServedEscortedAndRated : LogSkippingSystemTest() {
    override val name = "F21VisitServedEscortedAndRated"
    override val description = "Group of 2 is served at once, finishes eating, is escorted and rates POSITIVE."
    override val food = FOOD_RICE
    override val restaurants = ONE_SMALL_TABLE
    override val scenario = "foh/f21/scenario_group_of_two.json"
    override val logLevel = INFO
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Serving",
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Rice Bowl:2 to table 1 0 ticks after ordering.",
        )
        skipToAndAssert(
            "[INFO] FOH Finished Eating",
            "[INFO] FOH Finished Eating (R 1): 2 customers of group 1 have finished eating at table 1.",
        )
        assertNextLine(
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 1 from table 1 outside.",
        )
        assertNextLine(
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 1)
    }
}

/** P03: every customer of an EVENT group orders the event's favourite dish instead of the highest id. */
class P03EventOrdersFavouriteDish : LogSkippingSystemTest() {
    override val name = "P03EventOrdersFavouriteDish"
    override val description = "Event group of 4 on evening 4 is seated and orders Rice Bowl for everyone."
    override val food = FOOD_MENU
    override val restaurants = "foh/p03/restaurants_event_two_dishes.json"
    override val scenario = "foh/p03/scenario_event_favourite.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 77

    override suspend fun run() {
        skipToAndAssert(
            SEATING_PREFIX,
            GROUP_1_SEATED,
        )
        assertNextLine(ORDER_RICE_BOWL_FOR_FOUR)
    }
}

/**
 * F14: "the table is reserved for the whole evening" - the table of a regular group stays blocked
 * after that group has eaten and left, so a later group is never offered its seats.
 */
class F14AReservedTableIsBlockedForTheWholeEvening : LogSkippingSystemTest() {
    override val name = "F14AReservedTableIsBlockedForTheWholeEvening"
    override val description = "Regular group 1 leaves table 1 in tick 3, group 2 is still not offered it in tick 15."
    override val food = FOOD_RICE
    override val restaurants = "foh/f14/restaurants_two_tables_long_evening.json"
    override val scenario = "foh/f14/scenario_regular_then_group_of_four.json"
    override val logLevel = DEBUG
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(SEATING_PREFIX, GROUP_1_SEATED)
        skipToAndAssert(
            "[IMPORTANT] FOH Escorting",
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 2 customers of group 1 from table 1 outside.",
        )
        // Table 1 is empty from here on, only table 2 of the two 2-seat tables is free to merge,
        // so the group of four is offered no restaurant at all.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 15",
            "[IMPORTANT] Simulation: Tick 15 (1) started.",
        )
        assertNextLine("[DEBUG] Restaurant No Decision: Group 2 could not decide for a restaurant.")
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 1)
    }
}

/**
 * F16 + F30: "The front of house is immediately cleaned and tables are separated in preparation of
 * the next evening." Two 2-seat tables merged for a group of four on evening 1 are two separate
 * tables again on evening 2, so two pairs get one table each and nothing is merged.
 */
class F16MergedTablesAreSeparatedForTheNextEvening : LogSkippingSystemTest() {
    override val name = "F16MergedTablesAreSeparatedForTheNextEvening"
    override val description = "Tables 1 and 2 are merged on evening 1 and serve two separate pairs on evening 2."
    override val food = FOOD_RICE
    override val restaurants = "foh/f16/restaurants_two_small_tables.json"
    override val scenario = "foh/f16/scenario_merge_then_two_pairs.json"
    override val logLevel = INFO
    override val maxTicks = 48

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] FOH Merging Tables",
            "[INFO] FOH Merging Tables (R 1): For group 1 the tables 1,2 were merged into 1.",
        )
        assertNextLine(GROUP_1_SEATED)
        skipToAndAssert(
            "[IMPORTANT] Preparation: Preparation for evening 2",
            "[IMPORTANT] Preparation: Preparation for evening 2 starts.",
        )
        // No merging line this evening: the two pairs each take one of the separated tables.
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 1 by waitstaff 1.",
        )
        skipToAndAssert(
            SEATING_PREFIX,
            "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 2 by waitstaff 1.",
        )
        assertStatistics(1, cooked = 8, served = 8, delivered = 0, ratings = 3)
    }
}
