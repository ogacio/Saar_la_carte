package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val ONE_EVENING = 24
private const val COOKED_PREFIX = "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 2 meals of dish "
private const val ASSIGNMENT_PREFIX = "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type EXEC starts cooking"

/**
 * Pins the one-tick gap between a cook finishing a batch and taking the next one:
 * "A cook is free again in the tick after the one its batch finished in"
 * ([de.unisaarland.cs.se.selab.kitchen.Cook.isFree], `busyUntil < currentTick`).
 *
 * One EXEC cook, two orders placed in tick 3. Slow Roast (30 min = 3 ticks) occupies the cook from
 * tick 3 and finishes in tick 5. Quick Bowl is therefore assigned in tick 6, not tick 5, and lands
 * "3 ticks after ordering" rather than 2. Relaxing the comparison to `busyUntil <= currentTick`
 * moves both the assignment and the Meal Cooked line one tick earlier, which is what this test
 * detects.
 */
class F11ACookIsBusyUntilTheTickAfterItsBatchFinishes : LogSkippingSystemTest() {
    override val name = "F11ACookIsBusyUntilTheTickAfterItsBatchFinishes"
    override val description =
        "A cook that finishes a 3-tick batch in tick 5 takes the queued dish in tick 6, not in tick 5."
    override val food = "kitchen/f11_cook_free_next_tick/food_slow_and_quick.json"
    override val restaurants = "kitchen/f11_cook_free_next_tick/restaurants_single_exec_cook.json"
    override val scenario = "kitchen/f11_cook_free_next_tick/scenario_slow_then_quick.json"
    override val logLevel = IMPORTANT
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "$ASSIGNMENT_PREFIX 2 meals of dish Slow Roast",
            "$ASSIGNMENT_PREFIX 2 meals of dish Slow Roast based on order 1 for orders 1.",
        )
        // 30 minutes is ceil(30 / 10) = 3 ticks, so ordering in tick 3 finishes in tick 5.
        skipToAndAssert(COOKED_PREFIX, COOKED_PREFIX + "Slow Roast 2 ticks after ordering.")
        // The cook is still busy for the rest of tick 5: the queued Quick Bowl is not assigned yet.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 6 ",
            "[IMPORTANT] Simulation: Tick 6 (1) started.",
        )
        skipToAndAssert(
            "$ASSIGNMENT_PREFIX 2 meals of dish Quick Bowl",
            "$ASSIGNMENT_PREFIX 2 meals of dish Quick Bowl based on order 2 for orders 2.",
        )
        skipToAndAssert(COOKED_PREFIX, COOKED_PREFIX + "Quick Bowl 3 ticks after ordering.")
    }
}
