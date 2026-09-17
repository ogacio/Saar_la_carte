package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** The required root ingredients key is absent, so decoding fails. */
class RootMissingIngredients : ExampleSystemTestExtension() {
    override val name = "F03RootMissingIngredients"
    override val description = "The required root ingredients key is absent, so decoding fails."
    override val food = "f03/root/invalid/bad_root_missing_ingredients.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_missing_ingredients.json is invalid.")
    }
}

/** The required root recipes key is absent, so decoding fails. */
class RootMissingRecipes : ExampleSystemTestExtension() {
    override val name = "F03RootMissingRecipes"
    override val description = "The required root recipes key is absent, so decoding fails."
    override val food = "f03/root/invalid/bad_root_missing_recipes.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_missing_recipes.json is invalid.")
    }
}

/** An unknown top level key is rejected because unknown keys are not ignored. */
class RootExtraKey : ExampleSystemTestExtension() {
    override val name = "F03RootExtraKey"
    override val description = "An unknown top level key is rejected because unknown keys are not ignored."
    override val food = "f03/root/invalid/bad_root_extra_key.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_extra_key.json is invalid.")
    }
}

/** The root ingredients key is null even though it is non-nullable. */
class RootIngredientsNull : ExampleSystemTestExtension() {
    override val name = "F03RootIngredientsNull"
    override val description = "The root ingredients key is null even though it is non-nullable."
    override val food = "f03/root/invalid/bad_root_ingredients_null.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_ingredients_null.json is invalid.")
    }
}

/** The root recipes key is null even though it is non-nullable. */
class RootRecipesNull : ExampleSystemTestExtension() {
    override val name = "F03RootRecipesNull"
    override val description = "The root recipes key is null even though it is non-nullable."
    override val food = "f03/root/invalid/bad_root_recipes_null.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_recipes_null.json is invalid.")
    }
}

/** The root ingredients key holds an object instead of the required array. */
class RootIngredientsNotArray : ExampleSystemTestExtension() {
    override val name = "F03RootIngredientsNotArray"
    override val description = "The root ingredients key holds an object instead of the required array."
    override val food = "f03/root/invalid/bad_root_ingredients_not_array.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_ingredients_not_array.json is invalid.")
    }
}

/** The file's root is a JSON array instead of the required object. */
class RootNotObject : ExampleSystemTestExtension() {
    override val name = "F03RootNotObject"
    override val description = "The file's root is a JSON array instead of the required object."
    override val food = "f03/root/invalid/bad_root_not_object.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_not_object.json is invalid.")
    }
}

/** A zero byte food file is not valid JSON. */
class RootEmptyFile : ExampleSystemTestExtension() {
    override val name = "F03RootEmptyFile"
    override val description = "A zero byte food file is not valid JSON."
    override val food = "f03/root/invalid/bad_root_empty_file.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_empty_file.json is invalid.")
    }
}

/** The food file has broken JSON syntax, a trailing comma and an unclosed array. */
class RootMalformedJson : ExampleSystemTestExtension() {
    override val name = "F03RootMalformedJson"
    override val description = "The food file has broken JSON syntax, a trailing comma and an unclosed array."
    override val food = "f03/root/invalid/bad_root_malformed_json.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_root_malformed_json.json is invalid.")
    }
}
