package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val ONE_EVENING = 24
private const val ARRIVAL_PREFIX = "[INFO] Restaurant Arrival"

/** F15: two BAR tables that would otherwise merge for an oversized group never merge. */
class F15BarTablesNeverMerge : LogSkippingSystemTest() {
    override val name = "F15BarTablesNeverMerge"
    override val description =
        "Two BAR tables of size 2 total 4 seats, enough to merge for a group of 3, but BAR never " +
            "merges: no Merging Tables log, group sent away, negative rating."
    override val food = "foh/food_rice.json"
    override val restaurants = "foh/f16/restaurants_bar_only.json"
    override val scenario = "foh/f16/scenario_bar_group_of_three.json"
    override val logLevel = INFO
    override val maxTicks = ONE_EVENING

    override suspend fun run() {
        skipToAndAssert(
            ARRIVAL_PREFIX,
            "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.",
        )
        assertNextLine(
            "[INFO] FOH No Seating (R 1): Assigned waitstaff 1 but no table available, group 1 is sent away.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}
