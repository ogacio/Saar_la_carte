package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24

/**
 * Forum update 23: a group whose reservation failed rates "in the tick corresponding to the
 * restaurant's openingTickStart", which replaces the older wording "in the first tick of the
 * evening". The two only differ when the restaurant does not open at tick 1.
 *
 * This restaurant opens at tick 5, so the single 2-seat table cannot hold the group of three and
 * the NEGATIVE rating has to appear in tick 5. Every other failed-reservation test in the suite
 * uses a restaurant opening at tick 1, where the old and the new wording coincide, so a mutant
 * that hardcodes the first tick of the evening survives all of them and is caught only here.
 */
class P05AFailedReservationRatesAtTheOpeningTick : LogSkippingSystemTest() {
    override val name = "P05AFailedReservationRatesAtTheOpeningTick"
    override val description =
        "A restaurant opening at tick 5 rates its failed reservation in tick 5, not in tick 1."
    override val food = "foh/p05_backlash/food_rice.json"
    override val restaurants = "foh/f23_opening_tick/restaurants_opens_at_five.json"
    override val scenario = "foh/f23_opening_tick/scenario_oversized_regular.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH No Reserving",
            "[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 1.",
        )
        // Ticks 1 to 4 are before openingTickStart and log nothing at all.
        skipToAndAssert(
            "[IMPORTANT] Simulation: Tick 5 ",
            "[IMPORTANT] Simulation: Tick 5 (1) started.",
        )
        skipToAndAssert(
            "[INFO] Rating (R 1)",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
        assertStatistics(restaurantId = 1, cooked = 0, served = 0, delivered = 0, ratings = 1)
    }
}
