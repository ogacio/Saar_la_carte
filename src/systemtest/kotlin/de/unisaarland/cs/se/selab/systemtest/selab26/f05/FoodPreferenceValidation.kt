package de.unisaarland.cs.se.selab.systemtest.selab26.f05

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.ValidationSystemTest

// F05, Parse Customers: validation rules specific to a customer group's foodPreferences array —
// a preference may not favour/prefer/exclude the full set of dishes or ingredients, may not name
// an unknown dish, and favoriteDishes must not repeat an entry. Run against the reference first.

public const val SSCENARIO: String = "scenario"
public const val SFOOD: String = "gvalidation/biborka_mutants/food_three_dishes.json"
public const val SRESTAURANTS: String = "gvalidation/biborka_mutants/restaurants_one_asian.json"

/** A preference may not favour the set of all dishes. */
class FavouritesCoveringEveryDishIsInvalid : ValidationSystemTest(SSCENARIO, false) {
    override val name = "F05_FavouritesCoveringEveryDishIsInvalid"
    override val description = "A preference may not favour the set of all dishes."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandFavouritesAreEveryDish.json"
}

/** Favouring every dish but one is valid. */
class FavouritesCoveringAllButOneDishIsValid : ValidationSystemTest(SSCENARIO, true) {
    override val name = "F05_FavouritesCoveringAllButOneDishIsValid"
    override val description = "Favouring every dish but one is valid."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandFavouritesAllButOneDish.json"
}

/** A preference may not prefer the set of all ingredients. */
class PreferredCoveringEveryIngredientIsInvalid : ValidationSystemTest(SSCENARIO, false) {
    override val name = "F05_PreferredCoveringEveryIngredientIsInvalid"
    override val description = "A preference may not prefer the set of all ingredients."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandPreferredAreEveryIngredient.json"
}

/** Preferring every ingredient but one is valid. */
class PreferredCoveringAllButOneIngredientIsValid : ValidationSystemTest(SSCENARIO, true) {
    override val name = "F05_PreferredCoveringAllButOneIngredientIsValid"
    override val description = "Preferring every ingredient but one is valid."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandPreferredAllButOneIngredient.json"
}

/** A preference may not exclude the set of all ingredients. */
class ExcludedCoveringEveryIngredientIsInvalid : ValidationSystemTest(SSCENARIO, false) {
    override val name = "F05_ExcludedCoveringEveryIngredientIsInvalid"
    override val description = "A preference may not exclude the set of all ingredients."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandExcludedAreEveryIngredient.json"
}

/** A preference may not favour a dish no recipe has. */
class FavouriteDishUnknownIsInvalid : ValidationSystemTest(SSCENARIO, false) {
    override val name = "F05_FavouriteDishUnknownIsInvalid"
    override val description = "A preference may not favour a dish no recipe has."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandFavouriteDishUnknown.json"
}

/** favoriteDishes must not repeat a dish (uniqueItems). */
class FavouriteDishesDuplicatedIsInvalid : ValidationSystemTest(SSCENARIO, false) {
    override val name = "F05_FavouriteDishesDuplicatedIsInvalid"
    override val description = "favoriteDishes must not repeat a dish (uniqueItems)."
    override val food = SFOOD
    override val restaurants = SRESTAURANTS
    override val scenario = "gvalidation/biborka_mutants/scenario_gourmandFavouritesDuplicated.json"
}
