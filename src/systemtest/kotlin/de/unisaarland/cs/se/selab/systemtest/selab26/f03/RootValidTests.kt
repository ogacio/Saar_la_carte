package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

class RootBaseline : ExampleSystemTestExtension() {
    override val name = "F03RootBaseline"
    override val description = "A well formed food file with ingredients and recipes parses."
    override val food = "f03/root/valid/ok_baseline.json"
    override val restaurants = "f03/companions/restaurants_valid.json"
    override val scenario = "f03/companions/scenario_valid.json"
    override val logLevel = "DEBUG"
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_baseline.json successfully parsed and validated.")
    }
}

class RootSingleIngredientSingleRecipe : ExampleSystemTestExtension() {
    override val name = "F03RootSingleIngredientSingleRecipe"
    override val description = "The smallest possible valid food file, one ingredient and one recipe."
    override val food = "f03/root/valid/ok_single_ingredient_single_recipe.json"
    override val restaurants = "f03/companions/restaurants_valid.json"
    override val scenario = "f03/companions/scenario_valid.json"
    override val logLevel = "DEBUG"
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine(
            "[INFO] Initialization Info: ok_single_ingredient_single_recipe.json successfully parsed and validated.",
        )
    }
}
