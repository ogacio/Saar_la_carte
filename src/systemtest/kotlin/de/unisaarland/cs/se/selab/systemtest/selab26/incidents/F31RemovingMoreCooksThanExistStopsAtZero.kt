package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val IMPORTANT = "IMPORTANT"
private const val THREE_EVENINGS = 72
private const val ORDER_PREFIX = "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order "

/**
 * A STAFF incident may remove more cooks of a type than the restaurant has. The count has to stop
 * at zero rather than go negative, otherwise a later incident that adds a cook back only cancels
 * the overdraft instead of restoring the dish.
 *
 * The restaurant has one SAUCE cook. Evening 2 removes two of them, evening 3 adds one back. The
 * regular group favours the SAUCE dish, so each evening's order reports the state of the count:
 * Saucy Beef, then the Rice Bowl fallback (F13, no eligible cook), then Saucy Beef again. A count
 * left at -1 would keep evening 3 on Rice Bowl.
 */
class F31RemovingMoreCooksThanExistStopsAtZero : LogSkippingSystemTest() {
    override val name = "F31RemovingMoreCooksThanExistStopsAtZero"
    override val description =
        "Removing 2 of 1 SAUCE cooks stops at zero, so adding 1 back in the next evening restores the dish."
    override val food = "incidents/f31_over_removal/food_sauce_and_rice.json"
    override val restaurants = "incidents/f31_over_removal/restaurants_one_sauce_cook.json"
    override val scenario = "incidents/f31_over_removal/scenario_remove_two_add_one.json"
    override val logLevel = IMPORTANT
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(ORDER_PREFIX, ORDER_PREFIX + "1 of Saucy Beef:2 with waitstaff 1.")
        skipToAndAssert(
            "[IMPORTANT] Incident: Incident 1 ",
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 2.",
        )
        skipToAndAssert(ORDER_PREFIX, ORDER_PREFIX + "2 of Rice Bowl:2 with waitstaff 1.")
        skipToAndAssert(
            "[IMPORTANT] Incident: Incident 2 ",
            "[IMPORTANT] Incident: Incident 2 of type STAFF occurred before evening 3.",
        )
        skipToAndAssert(ORDER_PREFIX, ORDER_PREFIX + "3 of Saucy Beef:2 with waitstaff 1.")
    }
}
