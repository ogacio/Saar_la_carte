package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

/* F10, F11 and F12: which dish is started, by which cook, and when it is finished. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"
private const val IMPORTANT_LEVEL = "IMPORTANT"
private const val ASSIGNMENT = "[IMPORTANT] Kitchen Dish Assignment"
private const val COOKED = "[IMPORTANT] Kitchen Meal Cooked"
private const val COOK_1_EXEC_STARTS =
    "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type EXEC starts cooking "

/** F10: basic dishes are queued first, and only then the lower recipe id decides. */
class F10BasicDishesAreQueuedFirst : LogSkippingSystemTest() {
    override val name = "F10BasicDishesAreQueuedFirst"
    override val description = "The basic dish is started first although its recipe id is the higher one."
    override val food = "f10/food_basic_with_high_id.json"
    override val restaurants = "f10/restaurants_one_cook.json"
    override val scenario = "f10/scenario_one_group_two_dishes.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 2

    override suspend fun run() {
        skipToAndAssert(
            ASSIGNMENT,
            COOK_1_EXEC_STARTS +
                "1 meals of dish House Bowl based on order 1 for orders 1.",
        )
    }
}

/** F10: a cook takes every order of the dish it starts, so one batch names several orders. */
class F10OneBatchServesSeveralOrders : LogSkippingSystemTest() {
    override val name = "F10OneBatchServesSeveralOrders"
    override val description = "Two orders of the same dish are cooked in one batch by one cook."
    override val food = "f10/food_basic_with_high_id.json"
    override val restaurants = "f10/restaurants_one_cook.json"
    override val scenario = "f10/scenario_two_groups_one_dish.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 2

    override suspend fun run() {
        skipToAndAssert(
            ASSIGNMENT,
            COOK_1_EXEC_STARTS +
                "4 meals of dish House Bowl based on order 1 for orders 1,2.",
        )
    }
}

/** F11: a dish is only started by a cook of a type its recipe lists. */
class F11EachDishGoesToACookOfItsType : LogSkippingSystemTest() {
    override val name = "F11EachDishGoesToACookOfItsType"
    override val description = "Two dishes of different cook types are started by their own cook in one tick."
    override val food = "f11/food_two_cook_types.json"
    override val restaurants = "f11/restaurants_exec_and_sous.json"
    override val scenario = "f11/scenario_one_dish_per_type.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 2

    override suspend fun run() {
        skipToAndAssert(
            ASSIGNMENT,
            COOK_1_EXEC_STARTS +
                "2 meals of dish Exec Basic based on order 1 for orders 1.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 2 of type SOUS starts cooking " +
                "2 meals of dish Sous Dish based on order 2 for orders 2.",
        )
    }
}

/** F11: a cook starts another recipe only in a later tick, so the second dish waits. */
class F11ADishWaitsWhileItsOnlyCookIsBusy : LogSkippingSystemTest() {
    override val name = "F11ADishWaitsWhileItsOnlyCookIsBusy"
    override val description = "Two dishes of one cook type are started one after the other, not together."
    override val food = "f11/food_two_cook_types.json"
    override val restaurants = "f11/restaurants_exec_and_sous.json"
    override val scenario = "f11/scenario_two_dishes_one_cook.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 5

    override suspend fun run() {
        skipToAndAssert(
            ASSIGNMENT,
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type SOUS starts cooking " +
                "2 meals of dish Sous Dish based on order 1 for orders 1.",
        )
        skipToAndAssert(
            ASSIGNMENT,
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type SOUS starts cooking " +
                "2 meals of dish Sous Extra based on order 2 for orders 2.",
        )
    }
}

/** F12: a finished batch is one line, counted in ticks since the waiter took the order. */
class F12MealCookedReportsTheBatchAndTheWaitingTime : LogSkippingSystemTest() {
    override val name = "F12MealCookedReportsTheBatchAndTheWaitingTime"
    override val description = "A 40 minute dish ordered in tick 1 is reported as finished 3 ticks after ordering."
    override val food = "f12/food_quick_and_slow.json"
    override val restaurants = "f12/restaurants_one_cook.json"
    override val scenario = "f12/scenario_group_of_two.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 8

    override suspend fun run() {
        skipToAndAssert(
            COOKED,
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 1 meals of dish Slow Stew " +
                "3 ticks after ordering.",
        )
    }
}

/** F12: every tick the kitchen summarises its cooks, meals cooking, finished and servable. */
class F12KitchenStatusIsLoggedEveryTick : LogSkippingSystemTest() {
    override val name = "F12KitchenStatusIsLoggedEveryTick"
    override val description = "The kitchen summarises every tick, starting with the tick the group orders."
    override val food = "f12/food_quick_and_slow.json"
    override val restaurants = "f12/restaurants_one_cook.json"
    override val scenario = "f12/scenario_group_of_two.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 8

    override suspend fun run() {
        skipToAndAssert(
            "[DEBUG] Kitchen Status",
            "[DEBUG] Kitchen Status (R 1): 1 cooks were active cooking 1 and finishing 0 meals. " +
                "0 meals can be served by the waitstaff.",
        )
    }
}

/** F11: among eligible cooks the lowest-ranking one takes the dish, so PASTRY goes before EXEC. */
class F11TheLowestRankingEligibleCookTakesTheDish : LogSkippingSystemTest() {
    override val name = "F11TheLowestRankingEligibleCookTakesTheDish"
    override val description = "With an EXEC and a PASTRY cook both eligible, the PASTRY cook cooks the dish."
    override val food = "f11/food_exec_or_pastry.json"
    override val restaurants = "f11/restaurants_exec_and_pastry.json"
    override val scenario = "f11/scenario_group_of_two.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 4

    override suspend fun run() {
        skipToAndAssert(
            ASSIGNMENT,
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type PASTRY starts cooking " +
                "2 meals of dish Shared Dish based on order 1 for orders 1.",
        )
    }
}

/** F12: the meals finished this tick are logged by ascending cook id, not in staff order. */
class F12TheFinishedMealsAreLoggedInAscendingCookId : LogSkippingSystemTest() {
    override val name = "F12TheFinishedMealsAreLoggedInAscendingCookId"
    override val description = "Two cooks finish in the same tick and are reported by ascending cook id."
    override val food = "f11/food_pastry_basic_and_exec.json"
    override val restaurants = "f11/restaurants_two_tables_exec_and_pastry.json"
    override val scenario = "f11/scenario_two_dish_preferences.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 6

    override suspend fun run() {
        skipToAndAssert(
            COOKED,
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 2 meals of dish Pastry Basic " +
                "1 ticks after ordering.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 2 finished cooking 2 meals of dish Exec Plate " +
                "1 ticks after ordering.",
        )
    }
}

/** F12: ten minutes is zero ticks of waiting; the whole tick is asserted line by line. */
class F12ATenMinuteDishIsFinishedInTheSameTick : LogSkippingSystemTest() {
    override val name = "F12ATenMinuteDishIsFinishedInTheSameTick"
    override val description = "A ten minute dish is ordered, cooked and reported in the tick the group sits down."
    override val food = "f12/food_ten_minute.json"
    override val restaurants = "f12/restaurants_one_recipe.json"
    override val scenario = "f12/scenario_plain_group_of_two.json"
    override val logLevel = IMPORTANT_LEVEL
    override val maxTicks = 4

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] Simulation: Tick 1 ", "[IMPORTANT] Simulation: Tick 1 (1) started.")
        assertNextLine("[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.")
        assertNextLine(
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Quick Bowl:2 with waitstaff 1.",
        )
        assertNextLine(
            COOK_1_EXEC_STARTS +
                "2 meals of dish Quick Bowl based on order 1 for orders 1.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 2 meals of dish Quick Bowl " +
                "0 ticks after ordering.",
        )
    }
}
