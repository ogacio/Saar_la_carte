package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * Forum 345 (staff, worked example): a group with no free waiter at tick 21 (closing at 24) does
 * not get a second attempt at tick 22, because the restaurant is in its last 3 ticks by then. It
 * leaves and rates negative in tick 22, not seated.
 */
class F16RetryInLastThreeTicksIsRefused : LogSkippingSystemTest() {
    override val name = "F16RetryInLastThreeTicksIsRefused"
    override val description =
        "Group 2 finds no free waiter at tick 21; the retry at tick 22 is refused by the closing window."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f16c_closing_retry/restaurants.json"
    override val scenario = "foh/f16c_closing_retry/scenario.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] FOH No Seating",
            "[INFO] FOH No Seating (R 1): No free waitstaff available for group 2.",
        )
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 22 ",
            "[IMPORTANT] Simulation: Tick 22 (1) started.",
        )
        skipToAndAssert(
            "[INFO] Rating (R 1): Group 2",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}
