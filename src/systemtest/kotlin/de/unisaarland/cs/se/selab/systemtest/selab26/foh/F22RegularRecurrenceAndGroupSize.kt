package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val FIVE_EVENINGS = 120
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"
private const val SEATING_PREFIX = "[IMPORTANT] FOH Seating"
private const val GROUP_1_SEATED = "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1."

/** F22: a REGULAR with start 2 / period 2 visits only evenings 2 and 4, ordering for 3 each time. */
class F22RegularRecurrenceAndGroupSize : LogSkippingSystemTest() {
    override val name = "F22RegularRecurrenceAndGroupSize"
    override val description =
        "A REGULAR of 3 with start 2 / period 2 visits evenings 2 and 4 only, ordering Rice Bowl:3 each time."
    override val food = "foh/f22_dinnerforone/food_rice.json"
    override val restaurants = "foh/f22_dinnerforone/restaurants_open_five_evenings.json"
    override val scenario = "foh/f22_dinnerforone/scenario_regular_recurrence.json"
    override val logLevel = IMPORTANT
    override val maxTicks = FIVE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 1 starts.")
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 2 starts.")
        skipToAndAssert(SEATING_PREFIX, GROUP_1_SEATED)
        assertNextLine("[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:3 with waitstaff 1.")
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 3 starts.")
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 4 starts.")
        skipToAndAssert(SEATING_PREFIX, GROUP_1_SEATED)
        assertNextLine("[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 2 of Rice Bowl:3 with waitstaff 1.")
        skipToAndAssert(PREPARATION_PREFIX, "[IMPORTANT] Preparation: Preparation for evening 5 starts.")
        assertStatistics(1, cooked = 6, served = 6, delivered = 0, ratings = 2)
    }
}
