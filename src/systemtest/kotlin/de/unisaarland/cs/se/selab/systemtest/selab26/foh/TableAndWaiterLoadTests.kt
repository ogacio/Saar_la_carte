package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val SEATING = "[IMPORTANT] FOH Seating"
private const val MERGING = "[INFO] FOH Merging Tables"
private const val FOOD = "foh/food_rice.json"
private const val INFO_LEVEL = "INFO"
private const val GROUP_1_AT_TABLE_1 = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."

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
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.")
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
        skipToAndAssert(SEATING, "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 2 by waitstaff 1.")
    }
}
