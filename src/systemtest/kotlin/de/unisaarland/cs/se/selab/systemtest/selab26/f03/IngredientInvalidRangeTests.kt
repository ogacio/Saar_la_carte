package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** packagingVolume of zero violates the exclusive minimum of zero. */
class IngredientPackagingZero : ExampleSystemTestExtension() {
    override val name = "F03IngredientPackagingZero"
    override val description = "packagingVolume of zero violates the exclusive minimum of zero."
    override val food = "f03/ingredient/invalid/bad_ing_packaging_zero.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_packaging_zero.json is invalid.")
    }
}

/** A negative packagingVolume violates the exclusive minimum of zero. */
class IngredientPackagingNegative : ExampleSystemTestExtension() {
    override val name = "F03IngredientPackagingNegative"
    override val description = "A negative packagingVolume violates the exclusive minimum of zero."
    override val food = "f03/ingredient/invalid/bad_ing_packaging_negative.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_packaging_negative.json is invalid.")
    }
}

/** A fractional packagingVolume cannot decode into an integer field. */
class IngredientPackagingFractional : ExampleSystemTestExtension() {
    override val name = "F03IngredientPackagingFractional"
    override val description = "A fractional packagingVolume cannot decode into an integer field."
    override val food = "f03/ingredient/invalid/bad_ing_packaging_fractional.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_packaging_fractional.json is invalid.")
    }
}

/** The required packagingVolume key is absent. */
class IngredientPackagingMissing : ExampleSystemTestExtension() {
    override val name = "F03IngredientPackagingMissing"
    override val description = "The required packagingVolume key is absent."
    override val food = "f03/ingredient/invalid/bad_ing_packaging_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_packaging_missing.json is invalid.")
    }
}

/** A packagingVolume above Int.MAX_VALUE cannot decode into an integer field. */
class IngredientIntOverflow : ExampleSystemTestExtension() {
    override val name = "F03IngredientIntOverflow"
    override val description = "A packagingVolume above Int.MAX_VALUE cannot decode into an integer field."
    override val food = "f03/ingredient/invalid/bad_ing_int_overflow.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_int_overflow.json is invalid.")
    }
}

/** bestBefore of zero violates the exclusive minimum of zero. */
class IngredientBestBeforeZero : ExampleSystemTestExtension() {
    override val name = "F03IngredientBestBeforeZero"
    override val description = "bestBefore of zero violates the exclusive minimum of zero."
    override val food = "f03/ingredient/invalid/bad_ing_bestbefore_zero.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_bestbefore_zero.json is invalid.")
    }
}

/** A negative bestBefore violates the exclusive minimum of zero. */
class IngredientBestBeforeNegative : ExampleSystemTestExtension() {
    override val name = "F03IngredientBestBeforeNegative"
    override val description = "A negative bestBefore violates the exclusive minimum of zero."
    override val food = "f03/ingredient/invalid/bad_ing_bestbefore_negative.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_bestbefore_negative.json is invalid.")
    }
}

/** A fractional bestBefore cannot decode into an integer field. */
class IngredientBestBeforeFractional : ExampleSystemTestExtension() {
    override val name = "F03IngredientBestBeforeFractional"
    override val description = "A fractional bestBefore cannot decode into an integer field."
    override val food = "f03/ingredient/invalid/bad_ing_bestbefore_fractional.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_bestbefore_fractional.json is invalid.")
    }
}

/** Two ingredients sharing a name violate the ingredient name uniqueness rule. */
class IngredientDuplicateName : ExampleSystemTestExtension() {
    override val name = "F03IngredientDuplicateName"
    override val description = "Two ingredients sharing a name violate the ingredient name uniqueness rule."
    override val food = "f03/ingredient/invalid/bad_ing_dup_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_dup_name.json is invalid.")
    }
}

/** The same ingredient object listed twice violates name uniqueness. */
class IngredientDuplicateIdenticalObject : ExampleSystemTestExtension() {
    override val name = "F03IngredientDuplicateIdenticalObject"
    override val description = "The same ingredient object listed twice violates name uniqueness."
    override val food = "f03/ingredient/invalid/bad_dup_identical_ingredient.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_dup_identical_ingredient.json is invalid.")
    }
}

/** At least one ingredient must exist, so an empty ingredients array is rejected. */
class IngredientsEmptyArray : ExampleSystemTestExtension() {
    override val name = "F03IngredientsEmptyArray"
    override val description = "At least one ingredient must exist, so an empty ingredients array is rejected."
    override val food = "f03/ingredient/invalid/empty_ingredients_array.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_ingredients_array.json is invalid.")
    }
}

/** The second empty ingredients fixture is rejected for the same reason. */
class IngredientsEmptyArrayDuplicateFixture : ExampleSystemTestExtension() {
    override val name = "F03IngredientsEmptyArrayDuplicateFixture"
    override val description = "The second empty ingredients fixture is rejected for the same reason."
    override val food = "f03/ingredient/invalid/bad_ingredients_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ingredients_empty.json is invalid.")
    }
}

/** The missing ingredient rule fires before any recipe rule is reached. */
class IngredientsAndRecipesBothEmpty : ExampleSystemTestExtension() {
    override val name = "F03IngredientsAndRecipesBothEmpty"
    override val description = "The missing ingredient rule fires before any recipe rule is reached."
    override val food = "f03/ingredient/invalid/empty_both_arrays.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_both_arrays.json is invalid.")
    }
}

/** The second empty name fixture is rejected by the schema minimum length. */
class IngredientNameEmptyStringFixture : ExampleSystemTestExtension() {
    override val name = "F03IngredientNameEmptyStringFixture"
    override val description = "The second empty name fixture is rejected by the schema minimum length."
    override val food = "f03/ingredient/invalid/empty_string_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: empty_string_name.json is invalid.")
    }
}

/** The second null name fixture fails to decode into a non-nullable field. */
class IngredientNameNullFixture : ExampleSystemTestExtension() {
    override val name = "F03IngredientNameNullFixture"
    override val description = "The second null name fixture fails to decode into a non-nullable field."
    override val food = "f03/ingredient/invalid/null_required_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: null_required_name.json is invalid.")
    }
}
