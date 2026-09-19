package de.unisaarland.cs.se.selab.systemtest.selab26.menu

/* F13: which dishes a customer can still order, and what happens when none is left. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT_LEVEL = "IMPORTANT"
private const val ORDERING = "[IMPORTANT] FOH Ordering"
private const val NO_ORDERING = "[IMPORTANT] FOH No Ordering"
private const val ONE_PORTION_FOOD = "f13/food_one_portion.json"
private const val TWO_GROUPS = "f13/scenario_two_groups.json"
private const val ONE_TABLE = "f13/restaurants_one_table.json"
private const val GROUP_OF_TWO = "f13/scenario_group_of_two.json"
private const val THEY_LEAVE = "they leave the restaurant."

/** F13: the pantry is updated after each order, so the second customer can find nothing. */
class F13TheLastPortionLeavesTheSecondCustomerWithNothing : LogSkippingSystemTest() {
    override val name = "F13TheLastPortionLeavesTheSecondCustomerWithNothing"
    override val description = "Of two customers sharing one portion, one orders and one leaves."
    override val food = ONE_PORTION_FOOD
    override val restaurants = ONE_TABLE
    override val scenario = GROUP_OF_TWO
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 6

    override suspend fun run() {
        skipToAndAssert(
            ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:1 with waitstaff 1.",
        )
        skipToAndAssert(
            NO_ORDERING,
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 1 customers, " +
                THEY_LEAVE,
        )
    }
}

/** F13: ingredients reserved for another order count as gone before they are cooked. */
class F13ReservedIngredientsAreNotAvailable : LogSkippingSystemTest() {
    override val name = "F13ReservedIngredientsAreNotAvailable"
    override val description = "A portion reserved by an earlier group is gone for the next group."
    override val food = ONE_PORTION_FOOD
    override val restaurants = "f13/restaurants_two_tables.json"
    override val scenario = TWO_GROUPS
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 8

    override suspend fun run() {
        skipToAndAssert(
            ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:1 with waitstaff 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH No Ordering (R 1): Group 2",
            "[IMPORTANT] FOH No Ordering (R 1): Group 2 could not place an order for 2 customers, " +
                THEY_LEAVE,
        )
    }
}

/** F13: the highest recipe id wins, but only among the dishes that can still be ordered. */
class F13CustomersFallBackToTheNextAvailableDish : LogSkippingSystemTest() {
    override val name = "F13CustomersFallBackToTheNextAvailableDish"
    override val description = "When the special runs out the next group orders the other dish."
    override val food = "f13/food_scarce_special.json"
    override val restaurants = "f13/restaurants_two_tables_two_dishes.json"
    override val scenario = TWO_GROUPS
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(
            ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Chef Special:2 with waitstaff 1.",
        )
        skipToAndAssert(
            ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of House Bowl:2 with waitstaff 1.",
        )
        assertStatistics(1, cooked = 4, served = 4, delivered = 0, ratings = 2)
    }
}

/** F13: a dish needs its ingredients and at least one eligible cook to be orderable. */
class F13ADishWithoutAnEligibleCookIsNotOnTheMenu : LogSkippingSystemTest() {
    override val name = "F13ADishWithoutAnEligibleCookIsNotOnTheMenu"
    override val description = "With no cook for the only dish, both customers leave without ordering."
    override val food = "f13/food_vegetable_only.json"
    override val restaurants = ONE_TABLE
    override val scenario = GROUP_OF_TWO
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 6

    override suspend fun run() {
        skipToAndAssert(
            NO_ORDERING,
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 2 customers, " +
                THEY_LEAVE,
        )
    }
}

/** F13: an unavailable ingredient cannot be bought, and the dish returns when it ends. */
class F13AnUnavailableIngredientEmptiesTheMenuForOneEvening : LogSkippingSystemTest() {
    override val name = "F13AnUnavailableIngredientEmptiesTheMenuForOneEvening"
    override val description = "The dish is unorderable while its ingredient cannot be bought, and returns after."
    override val food = "f13/food_plenty.json"
    override val restaurants = ONE_TABLE
    override val scenario = "f13/scenario_unavailable_rice.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 30

    override suspend fun run() {
        skipToAndAssert(
            NO_ORDERING,
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 2 customers, " +
                THEY_LEAVE,
        )
        skipToAndAssert(
            ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 1 of Rice Bowl:2 with waitstaff 1.",
        )
    }
}
