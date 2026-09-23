package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val SEATING = "[IMPORTANT] FOH Seating"
private const val MERGING = "[INFO] FOH Merging Tables"
private const val FOOD = "foh/food_rice.json"
private const val INFO_LEVEL = "INFO"
private const val GROUP_1_AT_TABLE_1 = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."
private const val GROUP_2_AT_TABLE_2_BY_1 = "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1."

/**
 * F15 (forum thread 71): a merged table is split again as soon as the CASUAL group on it has left, not
 * only at the end of the evening. Group 1 (four) sits at tables 1 and 2 merged in tick 3 and is long
 * gone by tick 14, when two pairs arrive: each pair gets one of the two original tables.
 */
class F15MergedTableIsSplitWhenTheCasualGroupLeaves : LogSkippingSystemTest() {
    override val name = "F15MergedTableIsSplitWhenTheCasualGroupLeaves"
    override val description = "Tables 1 and 2 merged for a group of four serve two separate pairs later that evening."
    override val food = FOOD
    override val restaurants = "foh/f16/restaurants_two_small_tables.json"
    override val scenario = "foh/f15/scenario_merge_then_two_pairs_same_evening.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(MERGING, "[INFO] FOH Merging Tables (R 1): For group 1 the tables 1,2 were merged into 1.")
        assertNextLine(GROUP_1_AT_TABLE_1)
        skipToAndAssert("[IMPORTANT] Simulation: Tick 14", "[IMPORTANT] Simulation: Tick 14 (1) started.")
        // No merging line: the pairs take the separated tables.
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 1 by waitstaff 1.")
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 2 by waitstaff 1.")
    }
}

/**
 * F17, rule 2 of the waiter assignment: among the waiters with a current load below 10, the one with
 * the most customers seats the next group. Waiter 1 seated four customers in this tick and still has
 * SEATING actions left, so it also seats the group of three; waiter 2 stays without an id.
 */
class F17WaiterWithTheMostCustomersSeatsTheNextGroup : LogSkippingSystemTest() {
    override val name = "F17WaiterWithTheMostCustomersSeatsTheNextGroup"
    override val description = "Two waiters: after seating four, waiter 1 (load 4) also seats the next group of three."
    override val food = FOOD
    override val restaurants = "foh/f17/restaurants_two_waiters_four_and_three.json"
    override val scenario = "foh/f17/scenario_four_then_three.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(SEATING, GROUP_1_AT_TABLE_1)
        skipToAndAssert(SEATING, GROUP_2_AT_TABLE_2_BY_1)
    }
}

/**
 * F17: the current load drops again once the customers have left. Waiter 1 seats a group of ten in
 * tick 3 (load 10, not below 10 anymore). By tick 14 the group has eaten and was escorted out, so
 * waiter 1 is back at load 0 and, as the lower id in a tie, seats the pair. A waiter whose load never
 * drops would be skipped here, and the pair would go to a new waiter 2.
 */
class F17CurrentLoadDropsWhenTheCustomersLeave : LogSkippingSystemTest() {
    override val name = "F17CurrentLoadDropsWhenTheCustomersLeave"
    override val description = "Waiter 1 seats ten, they leave, and waiter 1 (not a new waiter 2) seats the next pair."
    override val food = FOOD
    override val restaurants = "foh/f17/restaurants_two_waiters_ten_and_two.json"
    override val scenario = "foh/f17/scenario_ten_leave_then_two.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(SEATING, GROUP_1_AT_TABLE_1)
        skipToAndAssert(
            "[IMPORTANT] FOH Escorting",
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 1 escorts 10 customers of group 1 from table 1 outside.",
        )
        skipToAndAssert(SEATING, GROUP_2_AT_TABLE_2_BY_1)
    }
}

/**
 * F17, rule 3 of the waiter assignment: once no waiter is below 10 customers, the least loaded one
 * seats the next group. Waiter 1 seats ten in tick 3; in tick 4 waiter 2 seats ten (rule 2, below 10)
 * and is then at its SEATING limit, so waiter 1 also takes the pair (rule 3). In tick 5 waiter 1 has
 * 12 and waiter 2 has 10 customers, and the next pair goes to waiter 2.
 */
class F17EverybodyBusyTheLeastLoadedWaiterSeats : LogSkippingSystemTest() {
    override val name = "F17EverybodyBusyTheLeastLoadedWaiterSeats"
    override val description = "With both waiters at 10 or more customers, the less loaded waiter 2 seats group 4."
    override val food = "foh/f17/food_rice_hundred.json"
    override val restaurants = "foh/f17/restaurants_two_waiters_ten_ten_two_two.json"
    override val scenario = "foh/f17/scenario_ten_ten_two_two.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 5

    override suspend fun run() {
        skipToAndAssert(SEATING, GROUP_1_AT_TABLE_1)
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 2.")
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 3 by waitstaff 1.")
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 4 seated at table 4 by waitstaff 2.")
    }
}

/**
 * F17 (specification adjustment of the current load): customers who leave because they cannot order
 * leave the load of their waiter at once, without being escorted. Rice cannot be bought on evening 1,
 * so the ten customers of group 1 find no dish and leave in tick 3. Waiter 1 is back at 0 and, as the
 * lower id in the tie with the waiter without an id, seats the pair in tick 4.
 */
class F17CustomersWhoCannotOrderLeaveTheLoad : LogSkippingSystemTest() {
    override val name = "F17CustomersWhoCannotOrderLeaveTheLoad"
    override val description = "Ten customers find no dish and leave; waiter 1 (load 0 again) seats the next pair."
    override val food = FOOD
    override val restaurants = "foh/f17/restaurants_two_waiters_ten_and_two.json"
    override val scenario = "foh/f17/scenario_ten_cannot_order_then_two.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 4

    override suspend fun run() {
        skipToAndAssert(SEATING, GROUP_1_AT_TABLE_1)
        skipToAndAssert(
            "[IMPORTANT] FOH No Ordering",
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 10 customers, " +
                "they leave the restaurant.",
        )
        skipToAndAssert(SEATING, GROUP_2_AT_TABLE_2_BY_1)
    }
}

/**
 * F17: "EVENTS do not count toward that load." Waiter 1 seats the event of ten in tick 5 of evening 4
 * and keeps a current load of 0, so it still wins the tie against the waiter without an id and seats the
 * pair in tick 6. If the event counted, waiter 1 would be at 10 and the pair would go to waiter 2.
 */
class F17EventCustomersDoNotCountTowardTheLoad : LogSkippingSystemTest() {
    override val name = "F17EventCustomersDoNotCountTowardTheLoad"
    override val description = "Waiter 1 seats an event of ten and, still at load 0, also seats the next pair."
    override val food = FOOD
    override val restaurants = "foh/f17/restaurants_event_two_waiters_ten_and_two.json"
    override val scenario = "foh/f17/scenario_event_of_ten_then_two.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 78

    override suspend fun run() {
        skipToAndAssert(SEATING, GROUP_1_AT_TABLE_1)
        skipToAndAssert(SEATING, GROUP_2_AT_TABLE_2_BY_1)
    }
}

/**
 * F17: the event manager seats an EVENT group with the waiters "in descending order of the current load".
 * In tick 3 of evening 4 waiter 1 seats a pair (load 2); the group of ten no longer fits into waiter 1's
 * SEATING limit and goes to waiter 2 (load 10). In tick 4 the event of four is seated by waiter 2 alone,
 * the higher load, although waiter 1 has the lower id and the lower load.
 */
class F17EventIsSeatedByTheMostLoadedWaiterFirst : LogSkippingSystemTest() {
    override val name = "F17EventIsSeatedByTheMostLoadedWaiterFirst"
    override val description = "Waiter 1 has 2 customers, waiter 2 has 10; the event of four is seated by waiter 2."
    override val food = FOOD
    override val restaurants = "foh/f17/restaurants_event_two_waiters_four_ten_two.json"
    override val scenario = "foh/f17/scenario_two_and_ten_then_event_of_four.json"
    override val logLevel = INFO_LEVEL
    override val maxTicks = 76

    override suspend fun run() {
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 3 by waitstaff 1.")
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 2 by waitstaff 2.")
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 2.")
    }
}
