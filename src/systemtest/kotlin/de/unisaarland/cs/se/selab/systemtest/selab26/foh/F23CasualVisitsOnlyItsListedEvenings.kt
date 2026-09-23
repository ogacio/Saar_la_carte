package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val THREE_EVENINGS = 72
private const val SERVING = "[IMPORTANT] Serving: Serving of evening "

/**
 * F23: "They have defined evenings (the visitingEvenings) at which they get food from restaurants."
 *
 * The group lists evenings 1 and 3, so it arrives in those two and not in evening 2. Skipping
 * straight from evening 2's start to the next arrival lands in evening 3; a mutant that ignores
 * visitingEvenings, or that treats the list as a start-and-period like a regular does, puts an
 * arrival in evening 2 and the assertion after the skip fails.
 */
class F23CasualVisitsOnlyItsListedEvenings : LogSkippingSystemTest() {
    override val name = "F23CasualVisitsOnlyItsListedEvenings"
    override val description =
        "A casual group listing evenings 1 and 3 arrives in both and stays away in evening 2."
    override val food = "delivery/browsing/food_three_durations.json"
    override val restaurants = "delivery/browsing/restaurants_two_tables_one_cook.json"
    override val scenario = "delivery/browsing/scenario_casual_evenings_one_and_three.json"
    override val logLevel = INFO
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(SERVING + "1 ", SERVING + "1 starts.")
        skipToAndAssert(
            "[INFO] Restaurant Arrival",
            "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.",
        )
        skipToAndAssert(SERVING + "2 ", SERVING + "2 starts.")
        // Nothing arrives in evening 2, so the next arrival must be the one in evening 3.
        skipToAndAssert(SERVING + "3 ", SERVING + "3 starts.")
        skipToAndAssert(
            "[INFO] Restaurant Arrival",
            "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.",
        )
        assertStatistics(restaurantId = 1, cooked = 4, served = 4, delivered = 0, ratings = 2)
    }
}
