package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ExampleSystemTestExtension

private const val DEBUG = "DEBUG"
private const val THREE_DISHES_FOOD = "gvalidation/stefan_gourmand/food_three_dishes.json"
private const val ONE_ASIAN_RESTAURANTS = "gvalidation/stefan_gourmand/restaurants_one_asian.json"
private const val SCENARIO_CHECKED = "scenario"

/**
 * A validation test: [food], [restaurants] and [scenario] are parsed in this order; the file named
 * [checked] ("food", "restaurants" or "scenario") is the one under test and must be valid or not.
 */
abstract class StefanValidationTest(
    private val checked: String,
    private val valid: Boolean,
) : ExampleSystemTestExtension() {
    override val logLevel = DEBUG
    override val maxTicks = 0

    override suspend fun run() {
        val files = listOf("food" to food, "restaurants" to restaurants, "scenario" to scenario)
        for ((kind, path) in files) {
            val file = path.substringAfterLast('/')
            if (kind != checked) {
                assertNextLine("[INFO] Initialization Info: $file successfully parsed and validated.")
                continue
            }
            val line = if (valid) {
                "[INFO] Initialization Info: $file successfully parsed and validated."
            } else {
                "[IMPORTANT] Initialization Info: $file is invalid."
            }
            assertNextLine(line)
            return
        }
    }
}

/** A preference may not favour the set of all dishes. */
class GourmandFavouritesAreEveryDish : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandFavouritesAreEveryDish"
    override val description = "A preference may not favour the set of all dishes."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandFavouritesAreEveryDish.json"
}

/** Favouring every dish but one is valid. */
class GourmandFavouritesAllButOneDish : StefanValidationTest(SCENARIO_CHECKED, true) {
    override val name = "F05GourmandFavouritesAllButOneDish"
    override val description = "Favouring every dish but one is valid."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandFavouritesAllButOneDish.json"
}

/** A preference may not prefer the set of all ingredients. */
class GourmandPreferredAreEveryIngredient : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandPreferredAreEveryIngredient"
    override val description = "A preference may not prefer the set of all ingredients."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandPreferredAreEveryIngredient.json"
}

/** Preferring every ingredient but one is valid. */
class GourmandPreferredAllButOneIngredient : StefanValidationTest(SCENARIO_CHECKED, true) {
    override val name = "F05GourmandPreferredAllButOneIngredient"
    override val description = "Preferring every ingredient but one is valid."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandPreferredAllButOneIngredient.json"
}

/** A preference may not exclude the set of all ingredients. */
class GourmandExcludedAreEveryIngredient : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandExcludedAreEveryIngredient"
    override val description = "A preference may not exclude the set of all ingredients."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandExcludedAreEveryIngredient.json"
}

/** A preference may not favour a dish no recipe has. */
class GourmandFavouriteDishUnknown : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandFavouriteDishUnknown"
    override val description = "A preference may not favour a dish no recipe has."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandFavouriteDishUnknown.json"
}

/** favoriteDishes must not repeat a dish (uniqueItems). */
class GourmandFavouritesDuplicated : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandFavouritesDuplicated"
    override val description = "favoriteDishes must not repeat a dish (uniqueItems)."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandFavouritesDuplicated.json"
}

/** The same ingredient cannot be preferred and excluded. */
class GourmandPreferredAndExcludedOverlap : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandPreferredAndExcludedOverlap"
    override val description = "The same ingredient cannot be preferred and excluded."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandPreferredAndExcludedOverlap.json"
}

/** The full-set rule applies to every preference object. */
class GourmandSecondPreferenceFavouritesAll : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandSecondPreferenceFavouritesAll"
    override val description = "The full-set rule applies to every preference object."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandSecondPreferenceFavouritesAll.json"
}

/** An event favourite must name an existing dish. */
class GourmandEventFavouriteUnknownDish : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandEventFavouriteUnknownDish"
    override val description = "An event favourite must name an existing dish."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandEventFavouriteUnknownDish.json"
}

/** An event needs a favourite for every type it considers. */
class GourmandEventMissesAFavouriteForAType : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandEventMissesAFavouriteForAType"
    override val description = "An event needs a favourite for every type it considers."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandEventMissesAFavouriteForAType.json"
}

/** Forum 352 (staff): an event favourite need not be a basic dish of that type. */
class GourmandEventFavouriteNotBasicIsValid : StefanValidationTest(SCENARIO_CHECKED, true) {
    override val name = "F05GourmandEventFavouriteNotBasicIsValid"
    override val description = "Forum 352: an event favourite need not be a basic dish of that type."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandEventFavouriteNotBasicIsValid.json"
}

/** An event's own preferences obey the full-set rule too. */
class GourmandEventPreferenceFavouritesAll : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandEventPreferenceFavouritesAll"
    override val description = "An event's own preferences obey the full-set rule too."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandEventPreferenceFavouritesAll.json"
}

/** A regular group's preferences obey the full-set rule too. */
class GourmandRegularFavouritesAll : StefanValidationTest(SCENARIO_CHECKED, false) {
    override val name = "F05GourmandRegularFavouritesAll"
    override val description = "A regular group's preferences obey the full-set rule too."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "gvalidation/stefan_gourmand/scenario_gourmandRegularFavouritesAll.json"
}
