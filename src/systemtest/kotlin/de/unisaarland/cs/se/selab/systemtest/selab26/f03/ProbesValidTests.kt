package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** PROBE: recipes: [] should be valid per spec bullet 3 / food.schema (no minItems)
 * — FoodParser.kt validateFileScope() line 139 requires recipes.isNotEmpty() — currently rejects */
class ProbeRecipesEmpty : ExampleSystemTestExtension() {
    override val name = "F03ProbeRecipesEmpty"
    override val description = "PROBE: recipes: [] should be valid per spec bullet 3, " +
        "but the parser currently rejects it."
    override val food = "f03/probes/valid/probe_recipes_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: probe_recipes_empty.json is invalid.")
    }
}

/** PROBE: recipe-ingredient unit disagrees with ingredient's declared unit (rice is "g") — no spec bullet or schema
 * rule forbids this — FoodParser.kt serialiseRecipeIngredients() line 128 rejects it */
class ProbeRecIngUnitMismatch : ExampleSystemTestExtension() {
    override val name = "F03ProbeRecIngUnitMismatch"
    override val description = "PROBE: recipe-ingredient unit disagrees with ingredient's declared unit, " +
        "which the parser currently rejects."
    override val food = "f03/probes/valid/probe_rec_ing_unit_mismatch.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: probe_rec_ing_unit_mismatch.json is invalid.")
    }
}

/** PROBE: stray key on a recipe-ingredient — recipe.schema has no additionalProperties:false there so schema
 * permits it, but RecipeIngredientDto + ignoreUnknownKeys=false fails to decode */
class ProbeRecIngExtraKey : ExampleSystemTestExtension() {
    override val name = "F03ProbeRecIngExtraKey"
    override val description = "PROBE: stray key on a recipe-ingredient, schema permits it but DTO decode fails."
    override val food = "f03/probes/valid/probe_rec_ing_extra_key.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: probe_rec_ing_extra_key.json is invalid.")
    }
}
