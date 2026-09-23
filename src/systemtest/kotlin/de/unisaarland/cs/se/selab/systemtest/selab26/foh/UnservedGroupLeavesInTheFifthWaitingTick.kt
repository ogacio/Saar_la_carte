package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

/** The line that starts [tick] of [evening]. */
private fun tick(tick: Int, evening: Int = 1) = "[IMPORTANT] Simulation: Tick $tick ($evening) started."

/**
 * F27, Customer - Waiting for Food: nobody in the group has been served after 5 waiting ticks, so
 * the whole group leaves and rates negative. Forum 335 (staff): the ordering tick is waiting tick 1.
 * The only cook is busy with group 1's stew (ticks 2-5), so group 2's soup (ordered in tick 3)
 * cannot be served before tick 8: group 2 leaves in tick 3 + 4 = 7 and rates NEGATIVE. Nothing else
 * is logged at INFO before it in tick 7. Run against the reference first.
 */
class UnservedGroupLeavesInTheFifthWaitingTick : LogSkippingSystemTest() {
    override val name = "F27_UnservedGroupLeavesInTheFifthWaitingTick"
    override val description = "Group 2, ordering in tick 3, leaves unserved in tick 7 and rates negative."
    override val food = "gvalidation/biborka_mutants/food_slow.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_two_pairs.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_second_group_waits_too_long.json"
    override val logLevel = "INFO"
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(tick(7), tick(7))
        assertNextLine(
            "[INFO] Restaurant No Eating (R 1): 2 customers of group 2 leave table 2 due to not being served."
        )
        skipToPrefix("[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating")
    }
}
