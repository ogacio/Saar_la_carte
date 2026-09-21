package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** recipes: [] — no file-level recipe minimum (duplicate of ok_recipes_empty, kept as its own row) */
class EmptyRecipesArray : ExampleSystemTestExtension() {
    override val name = "F03EmptyRecipesArray"
    override val description = "recipes: [] — no file-level recipe minimum (duplicate of ok_recipes_empty)."
    override val food = "f03/recipe/valid/empty_recipes_array.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: empty_recipes_array.json successfully parsed and validated.")
    }
}

/** recipe-ingredient unit: null — DTO field is nullable, treated as absent */
class NullRecipeIngUnit : ExampleSystemTestExtension() {
    override val name = "F03NullRecipeIngUnit"
    override val description = "recipe-ingredient unit: null — DTO field is nullable, treated as absent."
    override val food = "f03/recipe/valid/null_recipe_ing_unit.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: null_recipe_ing_unit.json successfully parsed and validated.")
    }
}

/** all 8 cookType values legal on a recipe, incl. EXEC */
class RecipeAllEightCooktypes : ExampleSystemTestExtension() {
    override val name = "F03RecipeAllEightCooktypes"
    override val description = "all 8 cookType values legal on a recipe, incl. EXEC."
    override val food = "f03/recipe/valid/ok_all_eight_cooktypes.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_all_eight_cooktypes.json successfully parsed and validated.")
    }
}

/** recipe-ingredient amount minimum (1) inclusive */
class RecipeAmountOne : ExampleSystemTestExtension() {
    override val name = "F03RecipeAmountOne"
    override val description = "recipe-ingredient amount minimum (1) inclusive."
    override val food = "f03/recipe/valid/ok_amount_one.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_amount_one.json successfully parsed and validated.")
    }
}

/** two recipes share dishName, neither basic — uniqueness is per-restaurant not file-level */
class RecipeDuplicateDishnameNoBasic : ExampleSystemTestExtension() {
    override val name = "F03RecipeDuplicateDishnameNoBasic"
    override val description = "two recipes share dishName, neither basic, uniqueness is per-restaurant not file-level."
    override val food = "f03/recipe/valid/ok_duplicate_dishname_no_basic.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine(
            "[INFO] Initialization Info: ok_duplicate_dishname_no_basic.json successfully parsed and validated."
        )
    }
}

/** duration lower bound (2) inclusive */
class RecipeDurationTwo : ExampleSystemTestExtension() {
    override val name = "F03RecipeDurationTwo"
    override val description = "duration lower bound (2) inclusive."
    override val food = "f03/recipe/valid/ok_duration_2.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_duration_2.json successfully parsed and validated.")
    }
}

/** duration upper bound (40) inclusive */
class RecipeDurationForty : ExampleSystemTestExtension() {
    override val name = "F03RecipeDurationForty"
    override val description = "duration upper bound (40) inclusive."
    override val food = "f03/recipe/valid/ok_duration_40.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_duration_40.json successfully parsed and validated.")
    }
}

/** recipe id minimum (0) inclusive */
class RecipeIdZero : ExampleSystemTestExtension() {
    override val name = "F03RecipeIdZero"
    override val description = "recipe id minimum (0) inclusive."
    override val food = "f03/recipe/valid/ok_id_zero.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_id_zero.json successfully parsed and validated.")
    }
}

/** no recipe has basicDishFor — file still valid (key absent) */
class RecipeNoBasicdishAnywhere : ExampleSystemTestExtension() {
    override val name = "F03RecipeNoBasicdishAnywhere"
    override val description = "no recipe has basicDishFor — file still valid."
    override val food = "f03/recipe/valid/ok_no_basicdish_anywhere.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_no_basicdish_anywhere.json successfully parsed and validated.")
    }
}

/** recipe-ingredient optional unit agrees with declared ingredient unit */
class RecipeIngredientUnitMatches : ExampleSystemTestExtension() {
    override val name = "F03RecipeIngredientUnitMatches"
    override val description = "recipe-ingredient optional unit agrees with declared ingredient unit."
    override val food = "f03/recipe/valid/ok_recipe_ingredient_unit_matches.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine(
            "[INFO] Initialization Info: ok_recipe_ingredient_unit_matches.json successfully parsed and validated."
        )
    }
}

/** recipes: [] — no file-level recipe minimum (spec bullet 3 only requires >=1 ingredient) */
class RecipesEmpty : ExampleSystemTestExtension() {
    override val name = "F03RecipesEmpty"
    override val description = "recipes: [] — no file-level recipe minimum (spec bullet 3 only requires " +
        ">=1 ingredient)."
    override val food = "f03/recipe/valid/ok_recipes_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_recipes_empty.json successfully parsed and validated.")
    }
}

/** one recipe lists the same ingredient name twice — not forbidden */
class RecipeSameIngredientTwice : ExampleSystemTestExtension() {
    override val name = "F03RecipeSameIngredientTwice"
    override val description = "one recipe lists the same ingredient name twice — not forbidden."
    override val food = "f03/recipe/valid/ok_same_ingredient_twice_in_recipe.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine(
            "[INFO] Initialization Info: ok_same_ingredient_twice_in_recipe.json successfully parsed and validated."
        )
    }
}

/** two basic dishes same type, different dishName — F-5 is per name not per type */
class RecipeTwoBasicsSameTypeDiffNames : ExampleSystemTestExtension() {
    override val name = "F03RecipeTwoBasicsSameTypeDiffNames"
    override val description = "two basic dishes same type, different dishName — F-5 is per name not per type."
    override val food = "f03/recipe/valid/ok_two_basics_same_type_diff_names.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine(
            "[INFO] Initialization Info: ok_two_basics_same_type_diff_names.json successfully parsed and validated."
        )
    }
}
