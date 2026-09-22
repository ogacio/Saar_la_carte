package de.unisaarland.cs.se.selab.systemtest.selab26.gkitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"

/**
 * From tests/giant/gaps/g1_sauce: a group favouring a SAUCE-cooked dish orders it while the SAUCE
 * cook is present (evenings 1-2), then silently falls back to the EXEC-cooked dish once a STAFF
 * incident removes the only SAUCE cook before evening 3 (F13, dish without an eligible cook is
 * dropped from the menu; the favourite does not override that).
 */
class GSauceCookRemovalFallback : LogSkippingSystemTest() {
    override val name = "GSauceCookRemovalFallback"
    override val description = "A favourite SAUCE dish is ordered until the only SAUCE cook is removed, " +
        "then the group falls back to the EXEC dish."
    override val food = "gkitchen/g1_sauce/food.json"
    override val restaurants = "gkitchen/g1_sauce/restaurants.json"
    override val scenario = "gkitchen/g1_sauce/scenario.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 96

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering (R 1): Group 1",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Saucy Beef:2 with waitstaff 1.",
        )
        skipToAndAssert(
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 3.",
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 3.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering (R 1): Group 1",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 3 of Rice Bowl:2 with waitstaff 1.",
        )
    }
}
