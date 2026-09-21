package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** recipes uniqueItems (schema) / F-2 duplicate recipe id */
class BadDupIdenticalRecipe : ExampleSystemTestExtension() {
    override val name = "F03BadDupIdenticalRecipe"
    override val description = "recipes uniqueItems (schema) / F-2 duplicate recipe id."
    override val food = "f03/recipe/invalid/bad_dup_identical_recipe.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_dup_identical_recipe.json is invalid.")
    }
}

/** F-5: exactly 1 recipe per basic dish name — two basic recipes share dishName "Rice Bowl" */
class BadRecBasicDupName : ExampleSystemTestExtension() {
    override val name = "F03BadRecBasicDupName"
    override val description =
        "F-5: exactly 1 recipe per basic dish name — two basic recipes share dishName 'Rice Bowl'."
    override val food = "f03/recipe/invalid/bad_rec_basic_dup_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_basic_dup_name.json is invalid.")
    }
}

/** F-5: rule is per dish name not per restaurant type — same dishName basic for two types still collides */
class BadRecBasicDupNameDiffType : ExampleSystemTestExtension() {
    override val name = "F03BadRecBasicDupNameDiffType"
    override val description =
        "F-5: rule is per dish name not per restaurant type — same dishName basic for two types still collides."
    override val food = "f03/recipe/invalid/bad_rec_basic_dup_name_diff_type.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_basic_dup_name_diff_type.json is invalid.")
    }
}

/** recipe.basicDishFor enum is exact-case — "asian" not a member */
class BadRecBasicDishLowercase : ExampleSystemTestExtension() {
    override val name = "F03BadRecBasicDishLowercase"
    override val description = "recipe.basicDishFor enum is exact-case — 'asian' not a member."
    override val food = "f03/recipe/invalid/bad_rec_basicdish_lowercase.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_basicdish_lowercase.json is invalid.")
    }
}

/** recipe.basicDishFor enum — "MEXICAN" not a restaurant type */
class BadRecBasicDishUnknown : ExampleSystemTestExtension() {
    override val name = "F03BadRecBasicDishUnknown"
    override val description = "recipe.basicDishFor enum — 'MEXICAN' not a restaurant type."
    override val food = "f03/recipe/invalid/bad_rec_basicdish_unknown.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_basicdish_unknown.json is invalid.")
    }
}

/** recipe.cookType uniqueItems — "SOUS" repeated */
class BadRecCookTypeDuplicate : ExampleSystemTestExtension() {
    override val name = "F03BadRecCookTypeDuplicate"
    override val description = "recipe.cookType uniqueItems — 'SOUS' repeated."
    override val food = "f03/recipe/invalid/bad_rec_cooktype_duplicate.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_cooktype_duplicate.json is invalid.")
    }
}

/** recipe.cookType minItems:1 — empty array */
class BadRecCookTypeEmpty : ExampleSystemTestExtension() {
    override val name = "F03BadRecCookTypeEmpty"
    override val description = "recipe.cookType minItems:1 — empty array."
    override val food = "f03/recipe/invalid/bad_rec_cooktype_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_cooktype_empty.json is invalid.")
    }
}

/** recipe.cookType enum is exact-case — "exec" not a member */
class BadRecCookTypeLowercase : ExampleSystemTestExtension() {
    override val name = "F03BadRecCookTypeLowercase"
    override val description = "recipe.cookType enum is exact-case — 'exec' not a member."
    override val food = "f03/recipe/invalid/bad_rec_cooktype_lowercase.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_cooktype_lowercase.json is invalid.")
    }
}

/** recipe.cookType is required */
class BadRecCookTypeMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecCookTypeMissing"
    override val description = "recipe.cookType is required."
    override val food = "f03/recipe/invalid/bad_rec_cooktype_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_cooktype_missing.json is invalid.")
    }
}

/** recipe.cookType enum — "CHEF" not a cook type */
class BadRecCookTypeUnknown : ExampleSystemTestExtension() {
    override val name = "F03BadRecCookTypeUnknown"
    override val description = "recipe.cookType enum — 'CHEF' not a cook type."
    override val food = "f03/recipe/invalid/bad_rec_cooktype_unknown.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_cooktype_unknown.json is invalid.")
    }
}

/** recipe.dishName minLength:1 — empty string */
class BadRecDishNameEmpty : ExampleSystemTestExtension() {
    override val name = "F03BadRecDishNameEmpty"
    override val description = "recipe.dishName minLength:1 — empty string."
    override val food = "f03/recipe/invalid/bad_rec_dishname_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_dishname_empty.json is invalid.")
    }
}

/** recipe.dishName is required */
class BadRecDishNameMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecDishNameMissing"
    override val description = "recipe.dishName is required."
    override val food = "f03/recipe/invalid/bad_rec_dishname_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_dishname_missing.json is invalid.")
    }
}

/** F-2: recipe ids must be unique */
class BadRecDupId : ExampleSystemTestExtension() {
    override val name = "F03BadRecDupId"
    override val description = "F-2: recipe ids must be unique."
    override val food = "f03/recipe/invalid/bad_rec_dup_id.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_dup_id.json is invalid.")
    }
}

/** recipe.duration minimum:2 — 1 below range */
class BadRecDuration1 : ExampleSystemTestExtension() {
    override val name = "F03BadRecDuration1"
    override val description = "recipe.duration minimum:2 — 1 below range."
    override val food = "f03/recipe/invalid/bad_rec_duration_1.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_duration_1.json is invalid.")
    }
}

/** recipe.duration maximum:40 — 41 above range */
class BadRecDuration41 : ExampleSystemTestExtension() {
    override val name = "F03BadRecDuration41"
    override val description = "recipe.duration maximum:40 — 41 above range."
    override val food = "f03/recipe/invalid/bad_rec_duration_41.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_duration_41.json is invalid.")
    }
}

/** recipe.duration must decode as integer — fractional value fails decode */
class BadRecDurationFractional : ExampleSystemTestExtension() {
    override val name = "F03BadRecDurationFractional"
    override val description = "recipe.duration must decode as integer — fractional value fails decode."
    override val food = "f03/recipe/invalid/bad_rec_duration_fractional.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_duration_fractional.json is invalid.")
    }
}

/** recipe.duration minimum:2 — negative below range */
class BadRecDurationNegative : ExampleSystemTestExtension() {
    override val name = "F03BadRecDurationNegative"
    override val description = "recipe.duration minimum:2 — negative below range."
    override val food = "f03/recipe/invalid/bad_rec_duration_negative.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_duration_negative.json is invalid.")
    }
}

/** recipe.duration minimum:2 — 0 below range */
class BadRecDurationZero : ExampleSystemTestExtension() {
    override val name = "F03BadRecDurationZero"
    override val description = "recipe.duration minimum:2 — 0 below range."
    override val food = "f03/recipe/invalid/bad_rec_duration_zero.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_duration_zero.json is invalid.")
    }
}

/** recipe additionalProperties:false — unknown key "spicy" */
class BadRecExtraKey : ExampleSystemTestExtension() {
    override val name = "F03BadRecExtraKey"
    override val description = "recipe additionalProperties:false — unknown key 'spicy'."
    override val food = "f03/recipe/invalid/bad_rec_extra_key.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_extra_key.json is invalid.")
    }
}

/** recipe.id must decode as integer — fractional value fails decode */
class BadRecIdFractional : ExampleSystemTestExtension() {
    override val name = "F03BadRecIdFractional"
    override val description = "recipe.id must decode as integer — fractional value fails decode."
    override val food = "f03/recipe/invalid/bad_rec_id_fractional.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_id_fractional.json is invalid.")
    }
}

/** recipe.id is required */
class BadRecIdMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecIdMissing"
    override val description = "recipe.id is required."
    override val food = "f03/recipe/invalid/bad_rec_id_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_id_missing.json is invalid.")
    }
}

/** recipe.id minimum:0 — negative not allowed */
class BadRecIdNegative : ExampleSystemTestExtension() {
    override val name = "F03BadRecIdNegative"
    override val description = "recipe.id minimum:0 — negative not allowed."
    override val food = "f03/recipe/invalid/bad_rec_id_negative.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_id_negative.json is invalid.")
    }
}

/** F-6: amount must decode as integer — fractional value fails decode */
class BadRecIngAmountFractional : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngAmountFractional"
    override val description = "F-6: amount must decode as integer — fractional value fails decode."
    override val food = "f03/recipe/invalid/bad_rec_ing_amount_fractional.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_amount_fractional.json is invalid.")
    }
}

/** recipe-ingredient.amount is required */
class BadRecIngAmountMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngAmountMissing"
    override val description = "recipe-ingredient.amount is required."
    override val food = "f03/recipe/invalid/bad_rec_ing_amount_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_amount_missing.json is invalid.")
    }
}

/** recipe-ingredient amount minimum:1 — negative not allowed */
class BadRecIngAmountNegative : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngAmountNegative"
    override val description = "recipe-ingredient amount minimum:1 — negative not allowed."
    override val food = "f03/recipe/invalid/bad_rec_ing_amount_negative.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_amount_negative.json is invalid.")
    }
}

/** recipe-ingredient amount minimum:1 — 0 not allowed */
class BadRecIngAmountZero : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngAmountZero"
    override val description = "recipe-ingredient amount minimum:1 — 0 not allowed."
    override val food = "f03/recipe/invalid/bad_rec_ing_amount_zero.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_amount_zero.json is invalid.")
    }
}

/** recipe-ingredient.name is required */
class BadRecIngNameMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngNameMissing"
    override val description = "recipe-ingredient.name is required."
    override val food = "f03/recipe/invalid/bad_rec_ing_name_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_name_missing.json is invalid.")
    }
}

/** F-4: recipe-ingredient name must resolve to a declared ingredient */
class BadRecIngUnknownName : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngUnknownName"
    override val description = "F-4: recipe-ingredient name must resolve to a declared ingredient."
    override val food = "f03/recipe/invalid/bad_rec_ing_unknown_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ing_unknown_name.json is invalid.")
    }
}

/** recipe.ingredients minItems:1 — empty array */
class BadRecIngredientsEmpty : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngredientsEmpty"
    override val description = "recipe.ingredients minItems:1 — empty array."
    override val food = "f03/recipe/invalid/bad_rec_ingredients_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ingredients_empty.json is invalid.")
    }
}

/** recipe.ingredients is required */
class BadRecIngredientsMissing : ExampleSystemTestExtension() {
    override val name = "F03BadRecIngredientsMissing"
    override val description = "recipe.ingredients is required."
    override val food = "f03/recipe/invalid/bad_rec_ingredients_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_rec_ingredients_missing.json is invalid.")
    }
}

/** recipe.cookType minItems:1 — empty array */
class EmptyCookTypeArray : ExampleSystemTestExtension() {
    override val name = "F03EmptyCookTypeArray"
    override val description = "recipe.cookType minItems:1 — empty array."
    override val food = "f03/recipe/invalid/empty_cooktype_array.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_cooktype_array.json is invalid.")
    }
}

/** recipe.ingredients minItems:1 — empty array */
class EmptyRecipeIngredientsArray : ExampleSystemTestExtension() {
    override val name = "F03EmptyRecipeIngredientsArray"
    override val description = "recipe.ingredients minItems:1 — empty array."
    override val food = "f03/recipe/invalid/empty_recipe_ingredients_array.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_recipe_ingredients_array.json is invalid.")
    }
}

/** recipe.dishName minLength:1 — empty string */
class EmptyStringDishName : ExampleSystemTestExtension() {
    override val name = "F03EmptyStringDishName"
    override val description = "recipe.dishName minLength:1 — empty string."
    override val food = "f03/recipe/invalid/empty_string_dishname.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_string_dishname.json is invalid.")
    }
}

/** recipe.basicDishFor type:string — null fails schema (SCHEMA-ONLY, parser currently reads null as absent) */
class NullBasicDishFor : ExampleSystemTestExtension() {
    override val name = "F03NullBasicDishFor"
    override val description = "recipe.basicDishFor type:string — null fails schema."
    override val food = "f03/recipe/invalid/null_basicdishfor.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: null_basicdishfor.json is invalid.")
    }
}

/** recipe.id is non-nullable — null fails decode */
class NullRequiredId : ExampleSystemTestExtension() {
    override val name = "F03NullRequiredId"
    override val description = "recipe.id is non-nullable — null fails decode."
    override val food = "f03/recipe/invalid/null_required_id.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: null_required_id.json is invalid.")
    }
}
