package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val EXEC_PLATE_BOUNDARY = "Exec Plate"
private const val SECOND_TICK_BOUNDARY = "2 (1)"
private const val FIRST_TICK_BOUNDARY = "1 (1)"
private const val SOUS_BOUNDARY = "SOUS"
private const val ASSIGNMENT_BOUNDARY = "[IMPORTANT] Kitchen Dish Assignment"
private const val COOKED_BOUNDARY = "[IMPORTANT] Kitchen Meal Cooked"
private const val TICK_BOUNDARY = "[IMPORTANT] Simulation: Tick "
private const val QUICK_BOWL_BOUNDARY = "Quick Bowl"
private const val ONE_RECIPE_BOUNDARY = "f12/restaurants_one_recipe.json"
private const val PLAIN_GROUP_BOUNDARY = "f12/scenario_plain_group_of_two.json"

/** Compare the entire kitchen trace, including tick boundaries, so early or extra batches fail. */
abstract class AddedCookingBoundaryTest : LogSkippingSystemTest() {
    override val logLevel = "IMPORTANT"

    protected suspend fun assertCookingTrace(expected: List<String>, includeServing: Boolean = false) {
        val actual = mutableListOf<String>()
        var tick = ""
        while (true) {
            val line = getNextLine() ?: break
            if (line.startsWith(TICK_BOUNDARY)) tick = line.removePrefix(TICK_BOUNDARY).removeSuffix(" started.")
            val kitchenLine = line.startsWith(ASSIGNMENT_BOUNDARY) || line.startsWith(COOKED_BOUNDARY)
            val servingLine = includeServing && line.startsWith("[IMPORTANT] FOH Serving (")
            if (kitchenLine || servingLine) {
                actual += "$tick | $line"
            }
        }
        if (actual != expected) throw SystemTestAssertionError("Expected kitchen trace $expected but got $actual")
    }

    protected fun assignment(tick: String, dish: String, order: Int, cook: Int = 1, type: String = "EXEC") =
        "$tick | $ASSIGNMENT_BOUNDARY (R 1): Cook $cook of type $type starts cooking " +
            "2 meals of dish $dish based on order $order for orders $order."

    protected fun cooked(tick: String, dish: String, delay: Int, cook: Int = 1) =
        "$tick | $COOKED_BOUNDARY (R 1): Cook $cook finished cooking 2 meals of dish $dish " +
            "$delay ticks after ordering."
}

/** F10: the lower recipe id wins inside one order after the basic dish has been cooked. */
class F10RecipeIdsBreakTiesWithinOneOrder : AddedCookingBoundaryTest() {
    override val name = "F10RecipeIdsBreakTiesWithinOneOrder"
    override val description = "Basic dish 9 precedes recipe 2 and then recipe 5 despite reversed meal input."
    override val food = "f10/food_recipe_id_priority.json"
    override val restaurants = "f10/restaurants_three_dishes.json"
    override val scenario = "f10/scenario_reverse_recipe_order.json"
    override val maxTicks = 3

    override suspend fun run() {
        val expected = listOf("Basic Bowl", "Low Plate", "High Plate").flatMapIndexed { index, dish ->
            val tick = "${index + 1} (1)"
            listOf(
                assignment(tick, dish, 1).replace("2 meals", "1 meals"),
                cooked(tick, dish, index).replace("2 meals", "1 meals"),
            )
        }
        assertCookingTrace(expected)
    }
}

/** F11: another eligible cook may start a later batch while the first cook is occupied. */
class F11LaterBatchOfTheSameDishUsesAnotherCook : AddedCookingBoundaryTest() {
    override val name = "F11LaterBatchOfTheSameDishUsesAnotherCook"
    override val description = "Two SOUS cooks start the same dish on consecutive ticks and finish separately."
    override val food = "f11/food_overlapping_batches.json"
    override val restaurants = "f11/restaurants_two_sous.json"
    override val scenario = "f11/scenario_later_same_dish.json"
    override val maxTicks = 4

    override suspend fun run() {
        assertCookingTrace(
            listOf(
                assignment(FIRST_TICK_BOUNDARY, QUICK_BOWL_BOUNDARY, 1, type = SOUS_BOUNDARY),
                assignment(SECOND_TICK_BOUNDARY, QUICK_BOWL_BOUNDARY, 2, cook = 2, type = SOUS_BOUNDARY),
                cooked("3 (1)", QUICK_BOWL_BOUNDARY, 2),
                cooked("4 (1)", QUICK_BOWL_BOUNDARY, 2, cook = 2),
            ),
        )
    }
}

/** F11: cook identity restarts at one on the second evening through the simulator's reset path. */
class F11CookIdsRestartThroughEveningPreparation : AddedCookingBoundaryTest() {
    override val name = "F11CookIdsRestartThroughEveningPreparation"
    override val description = "The EXEC cook receives ID 2 after PASTRY on evening 1, then ID 1 on evening 2."
    override val food = "f11/food_pastry_basic_and_exec.json"
    override val restaurants = "f11/restaurants_two_tables_exec_and_pastry.json"
    override val scenario = "f11/scenario_cook_ids_two_evenings.json"
    override val maxTicks = 27

    override suspend fun run() {
        assertCookingTrace(
            listOf(
                assignment(FIRST_TICK_BOUNDARY, "Pastry Basic", 1, type = "PASTRY"),
                assignment(SECOND_TICK_BOUNDARY, EXEC_PLATE_BOUNDARY, 2, cook = 2),
                cooked(SECOND_TICK_BOUNDARY, "Pastry Basic", 1),
                cooked("3 (1)", EXEC_PLATE_BOUNDARY, 1, cook = 2),
                assignment("2 (2)", EXEC_PLATE_BOUNDARY, 3),
                cooked("3 (2)", EXEC_PLATE_BOUNDARY, 1),
            ),
        )
    }
}

/** F12: the existing ten-minute test is complemented by eleven minutes and actual waiter handover. */
class F12ElevenMinuteBatchFinishesAndIsServedNextTick : AddedCookingBoundaryTest() {
    override val name = "F12ElevenMinuteBatchFinishesAndIsServedNextTick"
    override val description = "Neither meal finishes in tick 1; both finish and reach the table in tick 2."
    override val food = "f12/food_eleven_minute.json"
    override val restaurants = ONE_RECIPE_BOUNDARY
    override val scenario = PLAIN_GROUP_BOUNDARY
    override val maxTicks = 3

    override suspend fun run() {
        assertCookingTrace(
            listOf(
                assignment(FIRST_TICK_BOUNDARY, QUICK_BOWL_BOUNDARY, 1),
                cooked(SECOND_TICK_BOUNDARY, QUICK_BOWL_BOUNDARY, 1),
                "2 (1) | [IMPORTANT] FOH Serving (R 1): Waitstaff 1 serves Quick Bowl:2 " +
                    "to table 1 1 ticks after ordering.",
            ),
            includeServing = true,
        )
    }
}
