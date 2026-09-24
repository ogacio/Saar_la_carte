package de.unisaarland.cs.se.selab.systemtest.selab26.delivery

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * F24: "For deliveries, the meals queue until all meals of an order are ready and a driver is
 * free." The two members of this group order different dishes, a 10-minute Quick Bowl and a 40- * minute Slow Braise.
 * The Quick Bowl is cooked in the ordering tick, but the order only leaves
 * three ticks later, when the Slow Braise is done too - a driver never carries half an order.
 */
class F24AnOrderLeavesOnlyWhenEveryMealIsCooked : LogSkippingSystemTest() {
    override val name = "F24AnOrderLeavesOnlyWhenEveryMealIsCooked"
    override val description =
        "A delivery of one fast and one slow dish waits for the slow one before it is handed over."
    override val food = "delivery/f24_whole_order/food_fast_and_slow.json"
    override val restaurants = "delivery/f24_whole_order/restaurants_one_driver.json"
    override val scenario = "delivery/f24_whole_order/scenario_split_order.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 8 ",
            "[IMPORTANT] Simulation: Tick 8 (1) started.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Quick Bowl:1,Slow Braise:1.",
        )
        // The Quick Bowl is ready at once, but nothing is handed over in this tick.
        skipToAndAssert(
            "[IMPORTANT] Kitchen Meal Cooked",
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 1 meals of dish " +
                "Quick Bowl 0 ticks after ordering.",
        )
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 11 ",
            "[IMPORTANT] Simulation: Tick 11 (1) started.",
        )
        skipToAndAssert(
            "[IMPORTANT] Kitchen Meal Cooked",
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 2 finished cooking 1 meals of dish " +
                "Slow Braise 3 ticks after ordering.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Delivery",
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 1 serves Quick Bowl:1,Slow Braise:1 meals " +
                "to driver 1 for order 1.",
        )
    }
}
