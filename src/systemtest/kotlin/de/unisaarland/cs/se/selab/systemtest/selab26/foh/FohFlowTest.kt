package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"

/**
 * P02: one group through the whole front of the house.
 *
 * Where the other system tests pin one rule each, this one pins the ORDER of the steps: a group
 * arrives, is seated, orders, the kitchen assigns the dish and finishes it, a waiter serves it, the
 * guests eat, are escorted out and rate the restaurant. Only the beginning of each line is checked,
 * so the test says nothing about wording or timing - it fails when a step is missing altogether or
 * when two steps swap places, which is what no single-rule test can notice.
 *
 * It uses the same fixtures as the P05 tests: one restaurant, one cook, one CASUAL group of two
 * that arrives in tick 3 and is served four ticks after ordering.
 */
class P02FohFlowThroughEveryStep : LogSkippingSystemTest() {
    override val name = "P02FohFlowThroughEveryStep"
    override val description = "Arrival, seating, ordering, cooking, serving, eating, escorting and rating, in order."
    override val food = "foh/food_one_cook.json"
    override val restaurants = "foh/p05/restaurants_one_cook.json"
    override val scenario = "foh/p05/scenario_neutral_always.json"
    override val logLevel = DEBUG
    override val maxTicks = 12

    override suspend fun run() {
        skipToPrefix("[INFO] Restaurant Arrival (R 1): Group 1")
        skipToPrefix("[IMPORTANT] FOH Seating (R 1): Group 1")
        skipToPrefix("[IMPORTANT] FOH Ordering (R 1): Group 1")
        skipToPrefix("[IMPORTANT] Kitchen Dish Assignment (R 1):")
        skipToPrefix("[IMPORTANT] Kitchen Meal Cooked (R 1):")
        skipToPrefix("[IMPORTANT] FOH Serving (R 1): Waitstaff")
        skipToPrefix("[INFO] FOH Finished Eating (R 1):")
        skipToPrefix("[IMPORTANT] FOH Escorting (R 1): Waitstaff")
        skipToPrefix("[INFO] Rating (R 1): Group 1")
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 1)
    }
}

/**
 * The same flow with two steps swapped - and it MUST FAIL.
 *
 * A flow test that only skips forward can pass for the wrong reason: if the assertions were written
 * so loosely that any order satisfies them, the test would be green on a simulation that serves food
 * before anybody ordered. This class is the control that rules that out. It expects the serving line
 * before the ordering line, which cannot happen in a correct run, so a run that reports it as passed
 * means [P02FohFlowThroughEveryStep] is not actually checking the order.
 *
 * It is deliberately NOT registered in `SystemTestRegistration`: a registered failing test counts
 * against the mutation run. To use it, register it for one local run:
 *
 * and expect "End of log reached before '[IMPORTANT] FOH Ordering (R 1): Group 1'", because after
 * the serving line no further ordering line for group 1 exists. Then remove the registration again.
 */
class P02FohFlowBrokenOrderControl : LogSkippingSystemTest() {
    override val name = "P02FohFlowBrokenOrderControl"
    override val description = "Control: expects serving before ordering, which must make the test fail."
    override val food = "foh/food_one_cook.json"
    override val restaurants = "foh/p05/restaurants_one_cook.json"
    override val scenario = "foh/p05/scenario_neutral_always.json"
    override val logLevel = DEBUG
    override val maxTicks = 12

    override suspend fun run() {
        skipToPrefix("[INFO] Restaurant Arrival (R 1): Group 1")
        skipToPrefix("[IMPORTANT] FOH Seating (R 1): Group 1")
        // the two lines below are swapped on purpose
        skipToPrefix("[IMPORTANT] FOH Serving (R 1): Waitstaff")
        skipToPrefix("[IMPORTANT] FOH Ordering (R 1): Group 1")
        skipToPrefix("[IMPORTANT] Kitchen Dish Assignment (R 1):")
        skipToPrefix("[IMPORTANT] Kitchen Meal Cooked (R 1):")
        skipToPrefix("[INFO] FOH Finished Eating (R 1):")
        skipToPrefix("[IMPORTANT] FOH Escorting (R 1): Waitstaff")
        skipToPrefix("[INFO] Rating (R 1): Group 1")
    }
}
