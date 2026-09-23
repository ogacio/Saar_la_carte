package de.unisaarland.cs.se.selab.systemtest.selab26.f04

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ValidationSystemTest

// F04, Parse Restaurants: rules specific to the restaurants file — table id uniqueness within one
// restaurant, dish name / recipe reuse across restaurants, and basic dish coverage per restaurant
// type. Every test pins a rule from the spec or a staff forum answer. Run against the reference first.

public const val BFOOD: String = "gvalidation/biborka_mutants/food_asian_and_african.json"
public const val BSCENARIO: String = "gvalidation/biborka_mutants/scenario_empty.json"
public const val BRESTAURANTS: String = "restaurants"

/** Table ids are unique per restaurant, not globally. */
class SameTableIdsInTwoRestaurantsIsValid : ValidationSystemTest(BRESTAURANTS, true) {
    override val name = "F04_SameTableIdsInTwoRestaurantsIsValid"
    override val description = "Table ids are unique per restaurant, not globally."
    override val food = BFOOD
    override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSameTableIdsInTwoRestaurants.json"
    override val scenario = BSCENARIO
}

/** Two restaurants may list the same recipe. */
class SameRecipeInTwoRestaurantsIsValid : ValidationSystemTest(BRESTAURANTS, true) {
    override val name = "F04_SameRecipeInTwoRestaurantsIsValid"
    override val description = "Two restaurants may list the same recipe."
    override val food = BFOOD
    override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSameRecipeInTwoRestaurants.json"
    override val scenario = BSCENARIO
}

/** One restaurant lists the default Chicken Rice, another the adapted one of the same name. */
class AdaptedBasicDishInOtherRestaurantIsValid : ValidationSystemTest(BRESTAURANTS, true) {
    override val name = "F04_AdaptedBasicDishInOtherRestaurantIsValid"
    override val description =
        "One restaurant lists the default Chicken Rice, another the adapted one of the same name."
    override val food = BFOOD
    override val restaurants = "gvalidation/biborka_mutants/restaurants_indieAdaptedBasicDishInOtherRestaurant.json"
    override val scenario = BSCENARIO

    /** The second restaurant's type EUROPEAN has no basic dish. */
    class SecondTypeWithoutBasicDishIsInvalid : ValidationSystemTest(BRESTAURANTS, false) {
        override val name = "F04_SecondTypeWithoutBasicDishIsInvalid"
        override val description = "The second restaurant's type EUROPEAN has no basic dish."
        override val food = BFOOD
        override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSecondTypeWithoutBasicDish.json"
        override val scenario = BSCENARIO
    }

    /** Forum 202: AFRICAN has a basic dish in the food file even if no restaurant lists it. */
    class SecondTypeWithUnlistedBasicDishIsValid : ValidationSystemTest(BRESTAURANTS, true) {
        override val name = "F04_SecondTypeWithUnlistedBasicDishIsValid"
        override val description =
            "Forum 202: AFRICAN has a basic dish in the food file even if no restaurant lists it."
        override val food = BFOOD
        override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSecondTypeWithUnlistedBasicDish.json"
        override val scenario = BSCENARIO
    }

    /** Forum 202 (staff, 18 Sep): the recipes list may be empty. */
    class EmptyRecipesListIsValid : ValidationSystemTest(BRESTAURANTS, true) {
        override val name = "F04_EmptyRecipesListIsValid"
        override val description = "Forum 202 (staff, 18 Sep): the recipes list may be empty."
        override val food = BFOOD
        override val restaurants = "gvalidation/biborka_mutants/restaurants_indieEmptyRecipesList.json"
        override val scenario = BSCENARIO
    }

    /** Table ids must be unique inside one restaurant. */
    class DuplicateTableIdsInOneRestaurantIsInvalid : ValidationSystemTest(BRESTAURANTS, false) {
        override val name = "F04_DuplicateTableIdsInOneRestaurantIsInvalid"
        override val description = "Table ids must be unique inside one restaurant."
        override val food = BFOOD
        override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSecondRestaurantDuplicateTableIds.json"
        override val scenario = BSCENARIO
    }

    /** One restaurant may not list two recipes named Chicken Rice. */
    class DuplicateDishNameInOneRestaurantIsInvalid : ValidationSystemTest(BRESTAURANTS, false) {
        override val name = "F04_DuplicateDishNameInOneRestaurantIsInvalid"
        override val description = "One restaurant may not list two recipes named Chicken Rice."
        override val food = BFOOD
        override val restaurants = "gvalidation/biborka_mutants/restaurants_indieSecondRestaurantSameDishNameTwice.json"
        override val scenario = BSCENARIO
    }
}
