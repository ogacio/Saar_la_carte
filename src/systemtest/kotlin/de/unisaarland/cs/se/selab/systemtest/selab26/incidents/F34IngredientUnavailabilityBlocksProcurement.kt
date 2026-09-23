package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val FOUR_EVENINGS = 96
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"
private const val PROCURED_RICE = "[DEBUG] Pantry (R 1): Procured 10 g of rice from the supplier."

/**
 * F34: an UNAVAILABLE incident blocks the supplier from procuring an ingredient for the
 * evening it occurs plus its full duration, and procurement resumes automatically afterward.
 */
class F34IngredientUnavailabilityBlocksProcurement : LogSkippingSystemTest() {
    override val name = "F34IngredientUnavailabilityBlocksProcurement"
    override val description =
        "Rice is unavailable for evenings 2 and 3; procurement stops during the window and resumes in evening 4."
    override val food = "incidents/food_rice_ten.json"
    override val restaurants = "incidents/restaurants_ten_seats.json"
    override val scenario = "incidents/scenario_ingredient_unavailability.json"
    override val logLevel = DEBUG
    override val maxTicks = FOUR_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            PREPARATION_PREFIX,
            "[IMPORTANT] Preparation: Preparation for evening 1 starts.",
        )
        assertNextLine(PROCURED_RICE)
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type UNAVAILABLE occurred before evening 2.",
        )
        skipToAndAssert(
            PREPARATION_PREFIX,
            "[IMPORTANT] Preparation: Preparation for evening 2 starts.",
        )
        assertNoLineBeforePrefix(PROCURED_RICE, "[IMPORTANT] Preparation: Preparation for evening 3 starts.")
        assertCurrentLine("[IMPORTANT] Preparation: Preparation for evening 3 starts.")
        assertNoLineBeforePrefix(PROCURED_RICE, "[IMPORTANT] Preparation: Preparation for evening 4 starts.")
        assertCurrentLine("[IMPORTANT] Preparation: Preparation for evening 4 starts.")
        assertNextLine(PROCURED_RICE)
    }

    private suspend fun assertNoLineBeforePrefix(forbidden: String, stopPrefix: String) {
        while (true) {
            val line = getNextLine() ?: return
            if (line.startsWith(stopPrefix)) return
            if (line == forbidden) {
                throw SystemTestAssertionError("Unexpected procurement during the unavailability window: '$line'.")
            }
        }
    }
}
