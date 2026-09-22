package de.unisaarland.cs.se.selab.systemtest.selab26.f05

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val DEFAULT_FOOD = "f03/companions/food_valid.json"
private const val DEFAULT_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val SIMPLE_FOOD = "foh/food_rice.json"
private const val SIMPLE_RESTAURANTS = "f04/companions/restaurants_valid.json"
private const val BOUNDARY_RESTAURANTS = "f05/companions/restaurants_open_3_close_20.json"
private const val DEBUG_LEVEL = "DEBUG"

/** F05 additions that isolate cross-file, file-scope and boundary validation rules. */
abstract class AddedScenarioTest(
    private val fixture: String,
    private val valid: Boolean,
    private val foodFixture: String = DEFAULT_FOOD,
    private val restaurantFixture: String = DEFAULT_RESTAURANTS,
) : ExampleSystemTestExtension() {
    override val food = foodFixture
    override val restaurants = restaurantFixture
    override val scenario = "f05/${if (valid) "valid" else "invalid"}/$fixture.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine(parsedMessage(foodFixture))
        assertNextLine(parsedMessage(restaurantFixture))
        val level = if (valid) "INFO" else "IMPORTANT"
        val result = if (valid) "successfully parsed and validated" else "is invalid"
        assertNextLine("[$level] Initialization Info: $fixture.json $result.")
    }

    private fun parsedMessage(path: String): String =
        "[INFO] Initialization Info: ${path.substringAfterLast('/')} successfully parsed and validated."
}

/** Customer-group ids are global even when the groups have different types. */
class ScDuplicateIdAcrossGroupTypes : AddedScenarioTest("bad_sc_duplicate_id_across_group_types", false) {
    override val name = "F05ScDuplicateIdAcrossGroupTypes"
    override val description = "A casual and a regular group may not share an id."
}

/** Food-preference sizes may not exceed the number of customers in the group. */
class ScPreferenceSizesExceedGroup : AddedScenarioTest("bad_sc_preference_sizes_exceed_group", false) {
    override val name = "F05ScPreferenceSizesExceedGroup"
    override val description = "Food preferences covering three customers are invalid for a group of two."
}

/** Food-preference sizes may cover exactly every customer in the group. */
class ScPreferenceSizesEqualGroup : AddedScenarioTest("ok_sc_preference_sizes_equal_group", true) {
    override val name = "F05ScPreferenceSizesEqualGroup"
    override val description = "Food preferences covering three customers are valid for a group of three."
}

/** A preference cannot both exclude and prefer the same ingredient. */
class ScExcludedAndPreferredOverlap : AddedScenarioTest("bad_sc_excluded_and_preferred_overlap", false) {
    override val name = "F05ScExcludedAndPreferredOverlap"
    override val description = "The same ingredient cannot be excluded and preferred."
}

/** Excluding every ingredient from the food file is invalid. */
class ScExcludesCompleteIngredientSet : AddedScenarioTest(
    "bad_sc_excludes_complete_ingredient_set",
    false,
    "f05/companions/food_rice_beef.json",
    SIMPLE_RESTAURANTS,
) {
    override val name = "F05ScExcludesCompleteIngredientSet"
    override val description = "A preference may not exclude the complete ingredient set."
}

/** Ingredient names in preferences must resolve against the food file. */
class ScUnknownIngredientReference : AddedScenarioTest("bad_sc_unknown_ingredient_reference", false) {
    override val name = "F05ScUnknownIngredientReference"
    override val description = "A food preference may not reference an unknown ingredient."
}

/** Dish names in preferences must resolve against the food file. */
class ScUnknownDishReference : AddedScenarioTest("bad_sc_unknown_dish_reference", false) {
    override val name = "F05ScUnknownDishReference"
    override val description = "A food preference may not reference an unknown favourite dish."
}

/** A regular group may arrive exactly three ticks before closing. */
class ScRegularLastAllowedTick : AddedScenarioTest(
    "ok_sc_regular_last_allowed_tick",
    true,
    SIMPLE_FOOD,
    BOUNDARY_RESTAURANTS,
) {
    override val name = "F05ScRegularLastAllowedTick"
    override val description = "A regular group may visit at tick 17 when its restaurant closes at tick 20."
}

/** A regular group may not arrive within the last three ticks before closing. */
class ScRegularOneTickTooLate : AddedScenarioTest(
    "bad_sc_regular_one_tick_too_late",
    false,
    SIMPLE_FOOD,
    BOUNDARY_RESTAURANTS,
) {
    override val name = "F05ScRegularOneTickTooLate"
    override val description = "A regular group may not visit at tick 18 when its restaurant closes at tick 20."
}

/** A delivery is valid when ordering leaves exactly one tick in the ordering phase. */
class ScDeliveryBarelyInPhase : AddedScenarioTest(
    "ok_sc_delivery_barely_in_phase",
    true,
    SIMPLE_FOOD,
    SIMPLE_RESTAURANTS,
) {
    override val name = "F05ScDeliveryBarelyInPhase"
    override val description = "A six-kilometre delivery at tick 6 leaves exactly one ordering tick."
}

/** The same delivery is invalid one visiting tick earlier. */
class ScDeliveryOneTickTooEarly : AddedScenarioTest(
    "bad_sc_delivery_one_tick_too_early",
    false,
    SIMPLE_FOOD,
    SIMPLE_RESTAURANTS,
) {
    override val name = "F05ScDeliveryOneTickTooEarly"
    override val description = "A six-kilometre delivery at tick 5 leaves no ordering tick."
}

/** Every restaurant type considered by an event needs a favourite dish. */
class ScEventMissingFavouriteForType : AddedScenarioTest("bad_sc_event_missing_favourite_for_type", false) {
    override val name = "F05ScEventMissingFavouriteForType"
    override val description = "An event considering two restaurant types needs a favourite for both."
}

/** An event favourite must be a basic dish of the mapped restaurant type. */
class ScEventFavouriteForWrongType : AddedScenarioTest("bad_sc_event_favourite_for_wrong_type", false) {
    override val name = "F05ScEventFavouriteForWrongType"
    override val description = "A EUROPEAN basic dish cannot be the ASIAN event favourite."
}

/** Four customers meet the minimum event-group size. */
class ScEventMinimumSize : AddedScenarioTest("ok_sc_event_minimum_size", true) {
    override val name = "F05ScEventMinimumSize"
    override val description = "An event group with exactly four customers is valid."
}

/** Visiting evening zero is outside the valid one-based range. */
class ScVisitingEveningsContainsZero : AddedScenarioTest(
    "bad_sc_visiting_evenings_contains_zero",
    false,
    SIMPLE_FOOD,
    SIMPLE_RESTAURANTS,
) {
    override val name = "F05ScVisitingEveningsContainsZero"
    override val description = "A casual group's visiting evenings may not contain zero."
}
