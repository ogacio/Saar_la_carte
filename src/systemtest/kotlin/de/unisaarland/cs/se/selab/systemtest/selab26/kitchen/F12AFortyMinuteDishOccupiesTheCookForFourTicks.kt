package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val ONE_EVENING = 24
private const val COOKED = "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 1 finished cooking 2 meals of dish "

/**
 * F12 at the top of the duration range, where F12ATenMinuteDishIsFinishedInTheSameTick pins the
 * bottom. 40 minutes is ceil(40 / 10) = 4 ticks, so a dish ordered in tick 3 is reported "3 ticks
 * after ordering" - the batch occupies the cook for four ticks and finishes at the end of the
 * fourth.
 *
 * The queued Quick Bowl then shows the knock-on: it is 10 minutes, but it cannot start until the
 * cook is free, so it lands "4 ticks after ordering". A mutant that rounds the duration the other
 * way, or that frees the cook a tick early, moves both numbers.
 */
class F12AFortyMinuteDishOccupiesTheCookForFourTicks : LogSkippingSystemTest() {
    override val name = "F12AFortyMinuteDishOccupiesTheCookForFourTicks"
    override val description =
        "A 40 minute dish is reported 3 ticks after ordering and delays a 10 minute dish to 4."
    override val food = "delivery/browsing/food_three_durations.json"
    override val restaurants = "delivery/browsing/restaurants_two_tables_one_cook.json"
    override val scenario = "delivery/browsing/scenario_long_then_quick.json"
    override val logLevel = IMPORTANT
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(COOKED, COOKED + "Long Braise 3 ticks after ordering.")
        skipToAndAssert(COOKED, COOKED + "Quick Bowl 4 ticks after ordering.")
    }
}
