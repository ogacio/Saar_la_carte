package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/** F27: a group never served by ordered tick + 5 leaves as a whole, unlike an already-served group. */
class F27WholeGroupLeavesAtFive : LogSkippingSystemTest() {
    override val name = "F27WholeGroupLeavesAtFive"
    override val description =
        "Three groups order distinct slow dishes on one cook: group 1 is served, 2 and 3 give up at +5."
    override val food = "foh/f27_patience/food_slow_rice.json"
    override val restaurants = "foh/f27_patience/restaurants_one_cook_three_tables.json"
    override val scenario = "foh/f27_patience/scenario_three_groups_same_tick.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 5 ",
            "[IMPORTANT] Simulation: Tick 5 (1) started.",
        )
        skipToAndAssert(
            "[INFO] Restaurant No Eating",
            "[INFO] Restaurant No Eating (R 1): 2 customers of group 2 leave table 2 due to not being served.",
        )
        assertNextLine(
            "[INFO] Restaurant No Eating (R 1): 2 customers of group 3 leave table 3 due to not being served.",
        )
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 2",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
        assertNextLine(
            "[INFO] Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 2 negative ratings.",
        )
    }
}
