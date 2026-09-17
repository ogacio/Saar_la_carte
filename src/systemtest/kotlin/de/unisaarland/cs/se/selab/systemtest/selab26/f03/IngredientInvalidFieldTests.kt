package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** The required ingredient name key is absent. */
class IngredientNameMissing : ExampleSystemTestExtension() {
    override val name = "F03IngredientNameMissing"
    override val description = "The required ingredient name key is absent."
    override val food = "f03/ingredient/invalid/bad_ing_name_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_name_missing.json is invalid.")
    }
}

/** The ingredient name is null even though it is non-nullable. */
class IngredientNameNull : ExampleSystemTestExtension() {
    override val name = "F03IngredientNameNull"
    override val description = "The ingredient name is null even though it is non-nullable."
    override val food = "f03/ingredient/invalid/bad_ing_name_null.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_name_null.json is invalid.")
    }
}

/** An empty ingredient name violates the schema minimum length of one. */
class IngredientNameEmpty : ExampleSystemTestExtension() {
    override val name = "F03IngredientNameEmpty"
    override val description = "An empty ingredient name violates the schema minimum length of one."
    override val food = "f03/ingredient/invalid/bad_ing_name_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_name_empty.json is invalid.")
    }
}

/** The required ingredient unit key is absent. */
class IngredientUnitMissing : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitMissing"
    override val description = "The required ingredient unit key is absent."
    override val food = "f03/ingredient/invalid/bad_ing_unit_missing.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_missing.json is invalid.")
    }
}

/** An empty unit string is not a member of the unit enum. */
class IngredientUnitEmpty : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitEmpty"
    override val description = "An empty unit string is not a member of the unit enum."
    override val food = "f03/ingredient/invalid/bad_ing_unit_empty.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_empty.json is invalid.")
    }
}

/** The unit kg is not a member of the unit enum. */
class IngredientUnitUnknown : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitUnknown"
    override val description = "The unit kg is not a member of the unit enum."
    override val food = "f03/ingredient/invalid/bad_ing_unit_unknown.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_unknown.json is invalid.")
    }
}

/** The unit enum is case sensitive, so G is rejected while g is accepted. */
class IngredientUnitUppercaseG : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitUppercaseG"
    override val description = "The unit enum is case sensitive, so G is rejected while g is accepted."
    override val food = "f03/ingredient/invalid/bad_ing_unit_uppercase_g.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_uppercase_g.json is invalid.")
    }
}

/** The unit enum is case sensitive, so ML is rejected while mL is accepted. */
class IngredientUnitUppercaseMl : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitUppercaseMl"
    override val description = "The unit enum is case sensitive, so ML is rejected while mL is accepted."
    override val food = "f03/ingredient/invalid/bad_ing_unit_uppercase_ml.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_uppercase_ml.json is invalid.")
    }
}

/** The unit enum is case sensitive, so x is rejected while X is accepted. */
class IngredientUnitLowercaseX : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitLowercaseX"
    override val description = "The unit enum is case sensitive, so x is rejected while X is accepted."
    override val food = "f03/ingredient/invalid/bad_ing_unit_lowercase_x.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_unit_lowercase_x.json is invalid.")
    }
}

/** An unknown key on an ingredient object is rejected. */
class IngredientExtraKey : ExampleSystemTestExtension() {
    override val name = "F03IngredientExtraKey"
    override val description = "An unknown key on an ingredient object is rejected."
    override val food = "f03/ingredient/invalid/bad_ing_extra_key.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[IMPORTANT] Initialization Info: bad_ing_extra_key.json is invalid.")
    }
}
