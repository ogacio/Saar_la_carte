package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"

/** P05: a REGULAR whose reservation fails logs FOH No Reserving and rates negative in tick 1. */
class P05RegularFailedReservationRatesNegative : LogSkippingSystemTest() {
    override val name = "P05RegularFailedReservationRatesNegative"
    override val description =
        "A REGULAR group of 3 against a single 2-seat table fails its reservation and rates NEGATIVE " +
            "in tick 1."
    override val food = "foh/p05_backlash/food_rice.json"
    override val restaurants = "foh/p05_backlash/restaurants_one_small_table.json"
    override val scenario = "foh/p05_backlash/scenario_oversized_regular.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 1 starts.")
        assertNextLine("[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 1.")
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 1 ",
            "[IMPORTANT] Simulation: Tick 1 (1) started.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}
