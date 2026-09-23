package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

/** The line that starts [tick] of [evening]. */
private fun tick(tick: Int, evening: Int = 1) = "[IMPORTANT] Simulation: Tick $tick ($evening) started."

/**
 * F15, FOH - Table Merging & Tables: a merged table splits back into its originals once the group
 * has left, so a later arrival can be seated on any one of them individually. A group of six merges
 * tables 1, 2 and 3 into 1; after it has left, three pairs arrive in tick 14 and get tables 1, 2
 * and 3 separately (forum 71). Run against the reference first.
 */
class ThreeMergedTablesAreSplitAfterLeaving : LogSkippingSystemTest() {
    override val name = "F15_ThreeMergedTablesAreSplitAfterLeaving"
    override val description = "Three merged tables are three tables again once the group has left."
    override val food = "gvalidation/biborka_mutants/food_rice_bowl.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_three_pairs.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_six_then_three_pairs.json"
    override val logLevel = "INFO"
    override val maxTicks = 14
    val foh = "[IMPORTANT] FOH Seating"

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] FOH Merging Tables",
            "[INFO] FOH Merging Tables (R 1): For group 1 the tables 1,2,3 were merged into 1.",
        )
        skipToAndAssert(tick(14), tick(14))
        skipToAndAssert(
            foh,
            "[IMPORTANT] FOH Seating (R 1): Group 2 seated at table 1 by waitstaff 1."
        )
        skipToAndAssert(
            foh,
            "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 2 by waitstaff 1."
        )
        skipToAndAssert(
            foh,
            "[IMPORTANT] FOH Seating (R 1): Group 4 seated at table 3 by waitstaff 1."
        )
    }
}
