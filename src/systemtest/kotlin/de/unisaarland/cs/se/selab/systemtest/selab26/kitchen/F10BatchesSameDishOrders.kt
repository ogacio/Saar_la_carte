package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val ONE_EVENING = 24
private const val ORDERING_PREFIX = "[IMPORTANT] FOH Ordering"

/** F10: two groups ordering the same dish in one tick are cooked together, once. */
class F10BatchesSameDishOrders : LogSkippingSystemTest() {
    override val name = "F10BatchesSameDishOrders"
    override val description =
        "Groups 1 and 2 both order the Rice Bowl in tick 1; a single cook assignment and completion " +
            "log names both orders with the combined meal count."
    override val food = "kitchen/f10b_lostinthesauce/food_rice.json"
    override val restaurants = "kitchen/f10b_lostinthesauce/restaurants_two_tables.json"
    override val scenario = "kitchen/f10b_lostinthesauce/scenario_two_groups_same_dish.json"
    override val logLevel = IMPORTANT
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:2 with waitstaff 1.",
        )
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 2 placed order 2 of Rice Bowl:2 with waitstaff 1.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type EXEC starts cooking " +
                "4 meals of dish Rice Bowl based on order 1 for orders 1,2.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 4 meals " +
                "of dish Rice Bowl 0 ticks after ordering.",
        )
    }
}
