package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val CHICKEN = "chicken"
private const val RICE = "rice"

/**
 * F32: a RECIPE incident adapts exactly one ingredient, in every recipe that uses it. Two recipes need
 * chicken (10 g and 20 g) and one of them also rice (10 g); with ten seats the kitchen plans one portion
 * of each dish. Doubling chicken before evening 2 turns 30 g of chicken into 60 g, and rice stays 10 g.
 * Adapting rice as well, or only one of the recipes, changes one of the two procurement lines.
 */
class F32RecipeIncidentAdaptsOnlyItsIngredientInEveryRecipe : LogSkippingSystemTest() {
    override val name = "F32RecipeIncidentAdaptsOnlyItsIngredientInEveryRecipe"
    override val description = "RECIPE chicken +100 doubles chicken in both recipes and leaves rice unchanged."
    override val food = "incidents/food_chicken_rice_two_recipes.json"
    override val restaurants = "incidents/restaurants_ten_seats_two_recipes.json"
    override val scenario = "incidents/scenario_recipe_change_chicken.json"
    override val logLevel = "DEBUG"
    override val maxTicks = 48

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] Preparation",
            "[IMPORTANT] Preparation: Preparation for evening 1 starts.",
        )
        assertNextLine(procured(30, CHICKEN))
        assertNextLine(procured(10, RICE))
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type RECIPE occurred before evening 2.",
        )
        assertNextLine("[IMPORTANT] Preparation: Preparation for evening 2 starts.")
        assertNextLine(removed(30, CHICKEN))
        assertNextLine(removed(10, RICE))
        assertNextLine(procured(60, CHICKEN))
        assertNextLine(procured(10, RICE))
    }

    private fun procured(grams: Int, name: String) =
        "[DEBUG] Pantry (R 1): Procured $grams g of $name from the supplier."

    private fun removed(grams: Int, name: String) =
        "[DEBUG] Pantry (R 1): Removed $grams g of $name from the pantry."
}
