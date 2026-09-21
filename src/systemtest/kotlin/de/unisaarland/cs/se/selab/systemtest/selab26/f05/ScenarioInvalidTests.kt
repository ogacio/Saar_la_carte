package de.unisaarland.cs.se.selab.systemtest.selab26.f05

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val FOOD = "foh/food_rice.json"
private const val FOOD_PARSED = "[INFO] Initialization Info: food_rice.json successfully parsed and validated."
private const val DEBUG_LEVEL = "DEBUG"
private const val VALID_RESTAURANTS = "f04/companions/restaurants_valid.json"
private const val RESTAURANTS_PARSED =
    "[INFO] Initialization Info: restaurants_valid.json successfully parsed and validated."

/**
 * F05: a valid food and restaurants file with one broken scenario file. Every subclass names the
 * fixture and asserts that the scenario file is the one the simulation rejects.
 */
abstract class ScenarioInvalidTest(private val fixture: String) : ExampleSystemTestExtension() {
    override val food = FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f05/invalid/$fixture.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0

    override suspend fun run() {
        assertNextLine(FOOD_PARSED)
        assertNextLine(RESTAURANTS_PARSED)
        assertNextLine("[IMPORTANT] Initialization Info: $fixture.json is invalid.")
    }
}

/** A visitingTick of 22 is above the maximum of twenty-one. */
class ScVisitingTick22 : ScenarioInvalidTest("bad_sc_visiting_tick_22") {
    override val name = "F05ScVisitingTick22"
    override val description = "A visitingTick of 22 is above the maximum of twenty-one."
}

/** A casual group needs at least one restaurant type. */
class ScRestaurantTypesEmpty : ScenarioInvalidTest("bad_sc_restaurant_types_empty") {
    override val name = "F05ScRestaurantTypesEmpty"
    override val description = "A casual group needs at least one restaurant type."
}

/** MAYBE is not a member of the rating likelihood enum. */
class ScRatingUnknown : ScenarioInvalidTest("bad_sc_rating_unknown") {
    override val name = "F05ScRatingUnknown"
    override val description = "MAYBE is not a member of the rating likelihood enum."
}

/** A casual group cannot both sit at a table type and want a delivery. */
class ScCasualTableAndDistance : ScenarioInvalidTest("bad_sc_casual_table_and_distance") {
    override val name = "F05ScCasualTableAndDistance"
    override val description = "A casual group cannot both sit at a table type and want a delivery."
}

/** A casual group must declare its rating likelihood. */
class ScCasualNoLikelihood : ScenarioInvalidTest("bad_sc_casual_no_likelihood") {
    override val name = "F05ScCasualNoLikelihood"
    override val description = "A casual group must declare its rating likelihood."
}

/** An unknown key on a customer group is rejected. */
class ScExtraKey : ScenarioInvalidTest("bad_sc_extra_key") {
    override val name = "F05ScExtraKey"
    override val description = "An unknown key on a customer group is rejected."
}

/** The required root incidents key is absent. */
class ScMissingIncidents : ScenarioInvalidTest("bad_sc_missing_incidents") {
    override val name = "F05ScMissingIncidents"
    override val description = "The required root incidents key is absent."
}

/** Two customer groups sharing an id are rejected. */
class ScDuplicateGroupId : ScenarioInvalidTest("bad_sc_duplicate_group_id") {
    override val name = "F05ScDuplicateGroupId"
    override val description = "Two customer groups sharing an id are rejected."
}

/** A regular group visits a fixed restaurant and may not list restaurant types. */
class ScRegularWithTypes : ScenarioInvalidTest("bad_sc_regular_with_types") {
    override val name = "F05ScRegularWithTypes"
    override val description = "A regular group visits a fixed restaurant and may not list restaurant types."
}

/** A regular group bound to a restaurant that does not exist. */
class ScRegularUnknownRestaurant : ScenarioInvalidTest("bad_sc_regular_unknown_restaurant") {
    override val name = "F05ScRegularUnknownRestaurant"
    override val description = "A regular group bound to a restaurant that does not exist."
}

/** A visitingPeriod of 11 is above the maximum of ten. */
class ScRegularPeriod11 : ScenarioInvalidTest("bad_sc_regular_period_11") {
    override val name = "F05ScRegularPeriod11"
    override val description = "A visitingPeriod of 11 is above the maximum of ten."
}

/** An event group of three is below the minimum size of four. */
class ScEventSizeThree : ScenarioInvalidTest("bad_sc_event_size_three") {
    override val name = "F05ScEventSizeThree"
    override val description = "An event group of three is below the minimum size of four."
}

/** An eventEvening of 3 is below the minimum of four. */
class ScEventEveningThree : ScenarioInvalidTest("bad_sc_event_evening_three") {
    override val name = "F05ScEventEveningThree"
    override val description = "An eventEvening of 3 is below the minimum of four."
}

/** An event group must declare its favourite dishes. */
class ScEventNoFavourites : ScenarioInvalidTest("bad_sc_event_no_favourites") {
    override val name = "F05ScEventNoFavourites"
    override val description = "An event group must declare its favourite dishes."
}
