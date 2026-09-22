package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"
private const val INCIDENT_PREFIX = "[IMPORTANT] Incident"
private const val EVENING_1 = "[IMPORTANT] Preparation: Preparation for evening 1 starts."
private const val EVENING_2 = "[IMPORTANT] Preparation: Preparation for evening 2 starts."
private const val TWO_EVENINGS = 48
private const val CHICKEN = "chicken"
private const val RICE = "rice"

private fun procured(restaurantId: Int, grams: Int, ingredient: String) =
    "[DEBUG] Pantry (R $restaurantId): Procured $grams g of $ingredient from the supplier."

private fun removed(restaurantId: Int, grams: Int, ingredient: String) =
    "[DEBUG] Pantry (R $restaurantId): Removed $grams g of $ingredient from the pantry."

private fun restocked(restaurantId: Int) = "[INFO] Pantry (R $restaurantId): Restocked ingredients."

/** F32: a RECIPE incident on rice must not touch chicken, in either restaurant. */
class F32OnlyThatIngredientChanges : LogSkippingSystemTest() {
    override val name = "F32OnlyThatIngredientChanges"
    override val description =
        "A -50 RECIPE adaptation on rice halves the rice procurement in both restaurants " +
            "while chicken stays untouched, across a packaging-volume boundary."
    override val food = "incidents/f32b_chickenandrice/food_chicken_and_rice.json"
    override val restaurants = "incidents/f32b_chickenandrice/restaurants_two_asian.json"
    override val scenario = "incidents/f32b_chickenandrice/scenario_rice_adaptation.json"
    override val logLevel = DEBUG
    override val maxTicks = TWO_EVENINGS

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, EVENING_1)
        assertNextLine(procured(1, 5, CHICKEN))
        assertNextLine(procured(1, 12, RICE))
        assertNextLine(restocked(1))
        assertNextLine(procured(2, 5, CHICKEN))
        assertNextLine(procured(2, 12, RICE))
        assertNextLine(restocked(2))
        skipToAndAssert(
            INCIDENT_PREFIX,
            "[IMPORTANT] Incident: Incident 1 of type RECIPE occurred before evening 2.",
        )
        skipToAndAssert(PREPARATION_PREFIX, EVENING_2)
        assertNextLine(removed(1, 5, CHICKEN))
        assertNextLine(removed(1, 12, RICE))
        assertNextLine(procured(1, 5, CHICKEN))
        assertNextLine(procured(1, 6, RICE))
        assertNextLine(restocked(1))
        assertNextLine(removed(2, 5, CHICKEN))
        assertNextLine(removed(2, 12, RICE))
        assertNextLine(procured(2, 5, CHICKEN))
        assertNextLine(procured(2, 6, RICE))
        assertNextLine(restocked(2))
    }
}
