package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val SIX_EVENINGS = 144

/**
 * A 6-evening, 2-restaurant fixture stacking multiple incidents per evening (mixed types and
 * same-type pairs), deliberately out of id order in the JSON array, alongside a delivery load
 * and a driver staff change. Asserts strict ascending-id apply order for two of the busiest
 * evenings and that the final statistics stay internally consistent.
 */
class GiantIncidentOrderingStress : LogSkippingSystemTest() {
    override val name = "GiantIncidentOrderingStress"
    override val description =
        "6-evening incident-ordering stress: mixed-type and same-type incident stacks per evening, applied " +
            "strictly by ascending id regardless of JSON array order, alongside a delivery load."
    override val food = "giant/incident_stress/food.json"
    override val restaurants = "giant/incident_stress/restaurants.json"
    override val scenario = "giant/incident_stress/scenario.json"
    override val logLevel = DEBUG
    override val maxTicks = SIX_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 3 of type RECIPE occurred before evening 2.",
        )
        assertNextLine("[IMPORTANT] Incident: Incident 4 of type PACKAGING occurred before evening 2.")
        assertNextLine("[IMPORTANT] Incident: Incident 5 of type STAFF occurred before evening 2.")
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 6 of type STAFF occurred before evening 3.",
        )
        assertNextLine("[IMPORTANT] Incident: Incident 7 of type STAFF occurred before evening 3.")
        assertNextLine("[IMPORTANT] Incident: Incident 8 of type RECIPE occurred before evening 3.")
        assertNextLine("[IMPORTANT] Incident: Incident 9 of type RECIPE occurred before evening 3.")
        assertStatistics(1, cooked = 4, served = 0, delivered = 4, ratings = 4)
        assertStatistics(2, cooked = 1, served = 0, delivered = 1, ratings = 1)
    }
}
