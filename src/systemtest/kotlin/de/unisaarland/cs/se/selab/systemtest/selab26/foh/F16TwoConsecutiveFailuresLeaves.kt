package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24
private const val NO_SEATING_GROUP_3 = "[INFO] FOH No Seating (R 1): No free waitstaff available for group 3."
private const val EVENING_END_PREFIX = "[IMPORTANT] Serving: Serving of evening 1 ends."

/** F16: a group with no free waiter for two consecutive ticks leaves, with no third attempt. */
class F16TwoConsecutiveFailuresLeaves : LogSkippingSystemTest() {
    override val name = "F16TwoConsecutiveFailuresLeaves"
    override val description =
        "Group 3 finds no free waiter in ticks 3 and 4, then rates negative with no third seating attempt."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f16b_shortstaffed/restaurants_two_big_one_small.json"
    override val scenario = "foh/f16b_shortstaffed/scenario_two_strikes.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 3 ",
            "[IMPORTANT] Simulation: Tick 3 (1) started.",
        )
        skipToAndAssert("[INFO] FOH No Seating", NO_SEATING_GROUP_3)
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 4 ",
            "[IMPORTANT] Simulation: Tick 4 (1) started.",
        )
        skipToAndAssert("[INFO] FOH No Seating", NO_SEATING_GROUP_3)
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 3",
            "[INFO] Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
        assertNoMoreGroupThreeUntilEveningEnds()
    }

    private suspend fun assertNoMoreGroupThreeUntilEveningEnds() {
        while (true) {
            val line = getNextLine() ?: throw SystemTestAssertionError("End of log reached before evening end.")
            if (line.contains("group 3") || line.contains("Group 3")) {
                throw SystemTestAssertionError("Unexpected further mention of group 3: '$line'.")
            }
            if (line.startsWith(EVENING_END_PREFIX)) return
        }
    }
}
