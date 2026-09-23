package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

private const val DIR = "survivors/kingofthehill"
private const val FOOD_ONE_SAFFRON = "$DIR/food_one_saffron.json"
private const val RESTAURANTS_ONE_PAIR_TABLE = "$DIR/restaurants_one_pair_table.json"

/**
 * KingOfTheHill: customers order starting with the most excluded ingredients and then the least
 * favourite dishes. Both exclude nothing, so the customer with no favourite goes first and takes the
 * highest id, the only Saffron Rice; the other one's favourite is gone and the preferred tofu
 * decides: Tofu Bowl, not Egg Rice.
 */
class F18CustomerWithoutFavouritesOrdersFirst : SurvivorTest() {
    override val name = "F18CustomerWithoutFavouritesOrdersFirst"
    override val description = "The customer without preferences orders first and takes the only Saffron Rice."
    override val food = FOOD_ONE_SAFFRON
    override val restaurants = RESTAURANTS_ONE_PAIR_TABLE
    override val scenario = "$DIR/scenario_no_preference_orders_first.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Saffron Rice:1,Tofu Bowl:1 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: with one exclusion and one favourite each, the food preference listed first in the
 * JSON orders first and gets the only Saffron Rice. The second (excluding tofu) falls back to Egg
 * Rice; the other way round it would be Tofu Bowl.
 */
class F18JsonOrderBreaksTheOrderingTie : SurvivorTest() {
    override val name = "F18JsonOrderBreaksTheOrderingTie"
    override val description =
        "Equal exclusions and favourites: the first preference in the JSON gets the Saffron Rice."
    override val food = FOOD_ONE_SAFFRON
    override val restaurants = RESTAURANTS_ONE_PAIR_TABLE
    override val scenario = "$DIR/scenario_json_order_breaks_the_tie.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:1,Saffron Rice:1 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: a customer chooses the first matching favourite among the dishes still available,
 * so when the first favourite cannot be cooked the second favourite wins before preferred
 * ingredients are looked at.
 */
class F26SecondFavouriteWhenTheFirstIsUnavailable : SurvivorTest() {
    override val name = "F26SecondFavouriteWhenTheFirstIsUnavailable"
    override val description = "Saffron is unavailable: the second favourite Egg Rice beats the preferred tofu."
    override val food = FOOD_ONE_SAFFRON
    override val restaurants = RESTAURANTS_ONE_PAIR_TABLE
    override val scenario = "$DIR/scenario_second_favourite_when_first_is_gone.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:2 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: within a tick groups order by type (REGULAR, EVENT, CASUAL) and only then by id, so
 * regular 5 takes the only Saffron Rice and casual 1 falls back to its preferred tofu.
 */
class F18RegularOrdersBeforeACasualWithALowerId : SurvivorTest() {
    override val name = "F18RegularOrdersBeforeACasualWithALowerId"
    override val description =
        "Regular 5 orders before casual 1 in the same tick and takes the only Saffron Rice."
    override val food = FOOD_ONE_SAFFRON
    override val restaurants = "$DIR/restaurants_two_pair_tables.json"
    override val scenario = "$DIR/scenario_regular_before_casual.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/2 FOH Ordering (R 1): Group 5 placed order 1 of Egg Rice:1,Saffron Rice:1 with waitstaff 1.
            1/2 FOH Ordering (R 1): Group 1 placed order 2 of Tofu Bowl:2 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: among the dishes with the most preferred ingredients, the highest recipe id wins
 * (Egg Plate, 3, over Egg Rice, 2); Plain Noodles (6) is only the pick of the customer without
 * preferences.
 */
class F26PreferredIngredientTieGoesToTheHighestId : SurvivorTest() {
    override val name = "F26PreferredIngredientTieGoesToTheHighestId"
    override val description = "Egg Rice and Egg Plate both hold the preferred egg; Egg Plate has the higher id."
    override val food = "$DIR/food_preferred.json"
    override val restaurants = "$DIR/restaurants_tie.json"
    override val scenario = "$DIR/scenario_preferred_tie.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Plate:1,Plain Noodles:1 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: preferred ingredients are counted by enumeration, not quantity: two different
 * preferred ingredients beat one preferred ingredient in a larger amount.
 */
class F26PreferredIngredientsCountByKindNotAmount : SurvivorTest() {
    override val name = "F26PreferredIngredientsCountByKindNotAmount"
    override val description =
        "Egg Chive Rice (egg and chives) beats Big Omelette (5 egg) for egg and chives lovers."
    override val food = "$DIR/food_preferred.json"
    override val restaurants = "$DIR/restaurants_enumeration.json"
    override val scenario = "$DIR/scenario_preferred_enumeration.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Egg Chive Rice:1,Plain Noodles:1 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}

/**
 * KingOfTheHill: without preferences a customer takes the highest recipe id, not the last recipe in
 * the restaurant's list.
 */
class F26HighestRecipeIdEvenWhenListedOutOfOrder : SurvivorTest() {
    override val name = "F26HighestRecipeIdEvenWhenListedOutOfOrder"
    override val description = "Recipes 3, 12 and 7: customers without preferences order recipe 12."
    override val food = "$DIR/food_unsorted_ids.json"
    override val restaurants = "$DIR/restaurants_unsorted_ids.json"
    override val scenario = "$DIR/scenario_no_preference.json"
    override val maxTicks = 3

    override suspend fun run() {
        assertTrace(
            listOf(ORDERING, NO_ORDERING),
            """
            1/1 FOH Ordering (R 1): Group 1 placed order 1 of Tofu Rice:2 with waitstaff 1.
            """.trimIndent().lines(),
        )
    }
}
