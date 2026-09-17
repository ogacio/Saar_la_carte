package de.unisaarland.cs.se.selab.systemtest.selab26.f03

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val VALID_SCENARIO = "f03/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** The unit enum value g is accepted. */
class IngredientUnitG : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitG"
    override val description = "The unit enum value g is accepted."
    override val food = "f03/ingredient/valid/ok_unit_g.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_unit_g.json successfully parsed and validated.")
    }
}

/** The unit enum value mL is accepted. */
class IngredientUnitMl : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitMl"
    override val description = "The unit enum value mL is accepted."
    override val food = "f03/ingredient/valid/ok_unit_mL.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_unit_mL.json successfully parsed and validated.")
    }
}

/** The unit enum value X is accepted. */
class IngredientUnitX : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnitX"
    override val description = "The unit enum value X is accepted."
    override val food = "f03/ingredient/valid/ok_unit_X.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_unit_X.json successfully parsed and validated.")
    }
}

/** packagingVolume of 1 is the smallest legal value above the exclusive minimum. */
class IngredientPackagingOne : ExampleSystemTestExtension() {
    override val name = "F03IngredientPackagingOne"
    override val description = "packagingVolume of 1 is the smallest legal value above the exclusive minimum."
    override val food = "f03/ingredient/valid/ok_packaging_one.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_packaging_one.json successfully parsed and validated.")
    }
}

/** bestBefore of 1 is the smallest legal value above the exclusive minimum. */
class IngredientBestBeforeOne : ExampleSystemTestExtension() {
    override val name = "F03IngredientBestBeforeOne"
    override val description = "bestBefore of 1 is the smallest legal value above the exclusive minimum."
    override val food = "f03/ingredient/valid/ok_bestbefore_one.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_bestbefore_one.json successfully parsed and validated.")
    }
}

/** packagingVolume and bestBefore at Int.MAX_VALUE are still accepted. */
class IngredientLargeValues : ExampleSystemTestExtension() {
    override val name = "F03IngredientLargeValues"
    override val description = "packagingVolume and bestBefore at Int.MAX_VALUE are still accepted."
    override val food = "f03/ingredient/valid/ok_large_values.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_large_values.json successfully parsed and validated.")
    }
}

/** A long unicode ingredient name is accepted, only minLength 1 applies. */
class IngredientLongName : ExampleSystemTestExtension() {
    override val name = "F03IngredientLongName"
    override val description = "A long unicode ingredient name is accepted, only minLength 1 applies."
    override val food = "f03/ingredient/valid/ok_long_name.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_long_name.json successfully parsed and validated.")
    }
}

/** rice and Rice are distinct ingredients because name uniqueness is case sensitive. */
class IngredientCaseDistinctNames : ExampleSystemTestExtension() {
    override val name = "F03IngredientCaseDistinctNames"
    override val description = "rice and Rice are distinct ingredients because name uniqueness is case sensitive."
    override val food = "f03/ingredient/valid/ok_case_distinct_names.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_case_distinct_names.json successfully parsed and validated.")
    }
}

/** An ingredient declared but referenced by no recipe is not forbidden. */
class IngredientUnused : ExampleSystemTestExtension() {
    override val name = "F03IngredientUnused"
    override val description = "An ingredient declared but referenced by no recipe is not forbidden."
    override val food = "f03/ingredient/valid/ok_unused_ingredient.json"
    override val restaurants = VALID_RESTAURANTS
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_unused_ingredient.json successfully parsed and validated.")
    }
}
