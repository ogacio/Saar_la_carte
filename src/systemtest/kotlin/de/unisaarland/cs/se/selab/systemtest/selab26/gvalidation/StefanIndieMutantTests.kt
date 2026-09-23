package de.unisaarland.cs.se.selab.systemtest.selab26.gvalidation

private const val ASIAN_AND_AFRICAN_FOOD = "gvalidation/stefan_indie/food_asian_and_african.json"
private const val EMPTY_SCENARIO = "gvalidation/stefan_indie/scenario_empty.json"
private const val RESTAURANTS_CHECKED = "restaurants"

/** Table ids are unique per restaurant, not globally. */
class IndieSameTableIdsInTwoRestaurants : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieSameTableIdsInTwoRestaurants"
    override val description = "Table ids are unique per restaurant, not globally."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSameTableIdsInTwoRestaurants.json"
    override val scenario = EMPTY_SCENARIO
}

/** Two restaurants may list the same recipe. */
class IndieSameRecipeInTwoRestaurants : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieSameRecipeInTwoRestaurants"
    override val description = "Two restaurants may list the same recipe."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSameRecipeInTwoRestaurants.json"
    override val scenario = EMPTY_SCENARIO
}

/** One restaurant lists the default Chicken Rice, another the adapted one of the same name. */
class IndieAdaptedBasicDishInOtherRestaurant : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieAdaptedBasicDishInOtherRestaurant"
    override val description =
        "One restaurant lists the default Chicken Rice, another the adapted one of the same name."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieAdaptedBasicDishInOtherRestaurant.json"
    override val scenario = EMPTY_SCENARIO
}

/** The second restaurant's type EUROPEAN has no basic dish. */
class IndieSecondTypeWithoutBasicDish : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondTypeWithoutBasicDish"
    override val description = "The second restaurant's type EUROPEAN has no basic dish."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondTypeWithoutBasicDish.json"
    override val scenario = EMPTY_SCENARIO
}

/** Forum 202: AFRICAN has a basic dish in the food file even if no restaurant lists it. */
class IndieSecondTypeWithUnlistedBasicDish : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieSecondTypeWithUnlistedBasicDish"
    override val description = "Forum 202: AFRICAN has a basic dish in the food file even if no restaurant lists it."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondTypeWithUnlistedBasicDish.json"
    override val scenario = EMPTY_SCENARIO
}

/** Forum 202 (staff, 18 Sep): the recipes list may be empty. */
class IndieEmptyRecipesList : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieEmptyRecipesList"
    override val description = "Forum 202 (staff, 18 Sep): the recipes list may be empty."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieEmptyRecipesList.json"
    override val scenario = EMPTY_SCENARIO
}

/** Table ids must be unique inside one restaurant. */
class IndieSecondRestaurantDuplicateTableIds : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantDuplicateTableIds"
    override val description = "Table ids must be unique inside one restaurant."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantDuplicateTableIds.json"
    override val scenario = EMPTY_SCENARIO
}

/** One restaurant may not list two recipes named Chicken Rice. */
class IndieSecondRestaurantSameDishNameTwice : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantSameDishNameTwice"
    override val description = "One restaurant may not list two recipes named Chicken Rice."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantSameDishNameTwice.json"
    override val scenario = EMPTY_SCENARIO
}

/** Restaurant ids must be unique. */
class IndieDuplicateRestaurantIds : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieDuplicateRestaurantIds"
    override val description = "Restaurant ids must be unique."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieDuplicateRestaurantIds.json"
    override val scenario = EMPTY_SCENARIO
}

/** Restaurant names must be unique. */
class IndieDuplicateRestaurantNames : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieDuplicateRestaurantNames"
    override val description = "Restaurant names must be unique."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieDuplicateRestaurantNames.json"
    override val scenario = EMPTY_SCENARIO
}

/** openingTickStart < openingTickEnd holds for every restaurant. */
class IndieSecondRestaurantOpeningTimes : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantOpeningTimes"
    override val description = "openingTickStart < openingTickEnd holds for every restaurant."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantOpeningTimes.json"
    override val scenario = EMPTY_SCENARIO
}

/** Every restaurant may only list existing recipes. */
class IndieSecondRestaurantUnknownRecipe : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantUnknownRecipe"
    override val description = "Every restaurant may only list existing recipes."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantUnknownRecipe.json"
    override val scenario = EMPTY_SCENARIO
}

/** Every restaurant needs at least one cook. */
class IndieSecondRestaurantWithoutCooks : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantWithoutCooks"
    override val description = "Every restaurant needs at least one cook."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantWithoutCooks.json"
    override val scenario = EMPTY_SCENARIO
}

/** Every restaurant has at most one EXEC cook. */
class IndieSecondRestaurantTwoExec : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantTwoExec"
    override val description = "Every restaurant has at most one EXEC cook."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantTwoExec.json"
    override val scenario = EMPTY_SCENARIO
}

/** Every restaurant needs at least one table. */
class IndieSecondRestaurantNoTables : StefanValidationTest(RESTAURANTS_CHECKED, false) {
    override val name = "F04IndieSecondRestaurantNoTables"
    override val description = "Every restaurant needs at least one table."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieSecondRestaurantNoTables.json"
    override val scenario = EMPTY_SCENARIO
}

/** Table types are independent per restaurant. */
class IndieBarTableInOneRestaurantOnly : StefanValidationTest(RESTAURANTS_CHECKED, true) {
    override val name = "F04IndieBarTableInOneRestaurantOnly"
    override val description = "Table types are independent per restaurant."
    override val food = ASIAN_AND_AFRICAN_FOOD
    override val restaurants = "gvalidation/stefan_indie/restaurants_indieBarTableInOneRestaurantOnly.json"
    override val scenario = EMPTY_SCENARIO
}
