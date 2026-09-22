package de.unisaarland.cs.se.selab.systemtest.selab26.f04

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val DEFAULT_FOOD = "foh/food_rice.json"
private const val VALID_SCENARIO = "f04/companions/scenario_valid.json"
private const val DEBUG_LEVEL = "DEBUG"

/** F04 additions that isolate cross-file, file-scope and boundary validation rules. */
abstract class AddedRestaurantInvalidTest(
    private val fixture: String,
    private val foodFixture: String = DEFAULT_FOOD,
) : ExampleSystemTestExtension() {
    override val food = foodFixture
    override val restaurants = "f04/invalid/$fixture.json"
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        val foodName = foodFixture.substringAfterLast('/')
        assertNextLine("[INFO] Initialization Info: $foodName successfully parsed and validated.")
        assertNextLine("[IMPORTANT] Initialization Info: $fixture.json is invalid.")
    }
}

/** A known recipe does not hide a second recipe id that is absent from the food file. */
class ResKnownAndUnknownRecipe : AddedRestaurantInvalidTest("bad_res_known_and_unknown_recipe") {
    override val name = "F04ResKnownAndUnknownRecipe"
    override val description = "A restaurant containing recipe ids 1 and 999 is invalid when only 1 exists."
}

/** Globally valid recipes with equal dish names may not both belong to one restaurant. */
class ResDuplicateDishName : AddedRestaurantInvalidTest(
    "bad_res_duplicate_dish_name",
    "f04/companions/food_duplicate_dish_name.json",
) {
    override val name = "F04ResDuplicateDishName"
    override val description = "One restaurant may not contain two recipes with the same dish name."
}

/** Every represented restaurant type needs at least one corresponding basic dish. */
class ResMissingBasicDishForType : AddedRestaurantInvalidTest("bad_res_missing_basic_for_type") {
    override val name = "F04ResMissingBasicDishForType"
    override val description = "A EUROPEAN restaurant is invalid when the food file has only an ASIAN basic dish."
}

/** A restaurant must employ at least one cook across all cook types. */
class ResAllCooksZero : AddedRestaurantInvalidTest("bad_res_all_cooks_zero") {
    override val name = "F04ResAllCooksZero"
    override val description = "A restaurant with zero cooks of every type is invalid."
}

/** The smallest positive opening interval is valid. */
class ResAdjacentOpeningTicks : ExampleSystemTestExtension() {
    override val name = "F04ResAdjacentOpeningTicks"
    override val description = "Opening at tick 1 and closing at tick 2 is valid."
    override val food = DEFAULT_FOOD
    override val restaurants = "f04/valid/ok_res_adjacent_opening_ticks.json"
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: food_rice.json successfully parsed and validated.")
        assertNextLine(
            "[INFO] Initialization Info: ok_res_adjacent_opening_ticks.json successfully parsed and validated.",
        )
    }
}

/** One cook, waiter, table and recipe meet each positive cardinality boundary. */
class ResMinimumCardinalities : ExampleSystemTestExtension() {
    override val name = "F04ResMinimumCardinalities"
    override val description = "Exactly one cook, waiter, table and recipe form a valid restaurant."
    override val food = DEFAULT_FOOD
    override val restaurants = "f04/valid/ok_res_minimum_cardinalities.json"
    override val scenario = VALID_SCENARIO
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: food_rice.json successfully parsed and validated.")
        assertNextLine(
            "[INFO] Initialization Info: ok_res_minimum_cardinalities.json successfully parsed and validated.",
        )
    }
}
