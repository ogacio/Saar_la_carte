package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

// F26, Customer - Food Preferences: how the ordering rules pick a dish when a preference applies
// to several members at once, when there is no preference at all, and when a dish's count of
// preferred ingredients has to outrank the highest recipe id. Run against the reference first.

public const val PREFERENCE_LOG_LEVEL_IMPORTANT: String = "IMPORTANT"
public const val IMPORTANT_FOH_ORDERING: String = "[IMPORTANT] FOH Ordering"

/**
 * A preference of size 2 applies to both customers: neither orders the chicken dish (id 2), both
 * order Egg Rice. If the size were ignored, the second customer would take the highest recipe id,
 * Chicken Rice, instead.
 */
class PreferenceSizeCoversEveryMemberItApplesTo : LogSkippingSystemTest() {
    override val name = "F26_PreferenceSizeCoversEveryMemberItApplesTo"
    override val description = "Both members of a size-2 preference avoid chicken and order Egg Rice."
    override val food = "gvalidation/biborka_mutants/food_egg_or_chicken.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_two_dishes.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_pair_excluding_chicken.json"
    override val logLevel = PREFERENCE_LOG_LEVEL_IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            IMPORTANT_FOH_ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:2 with waitstaff 1.",
        )
    }
}

/** Customers without preferences take the dish with the highest recipe id (Plain Rice, id 3). */
class NoPreferenceTakesHighestRecipeId : LogSkippingSystemTest() {
    override val name = "F26_NoPreferenceTakesHighestRecipeId"
    override val description = "A pair without preferences orders the highest recipe id."
    override val food = "gvalidation/biborka_mutants/food_three_dishes.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_one_asian.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_kingOfTheHillNoPreferenceTakesHighestRecipeId.json"
    override val logLevel = PREFERENCE_LOG_LEVEL_IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            IMPORTANT_FOH_ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Plain Rice:2 with waitstaff 1.",
        )
    }
}

/** Preferring egg makes Egg Rice (id 1, one preferred ingredient) win over Plain Rice (id 3, none). */
class MostPreferredIngredientsBeatHighestRecipeId : LogSkippingSystemTest() {
    override val name = "F26_MostPreferredIngredientsBeatHighestRecipeId"
    override val description = "The dish with the most preferred ingredients wins over the highest id."
    override val food = "gvalidation/biborka_mutants/food_three_dishes.json"
    override val restaurants = "gvalidation/biborka_mutants/restaurants_one_asian.json"
    override val scenario = "gvalidation/biborka_mutants/scenario_kingOfTheHillMostPreferredIngredientsBeatHighestId." +
        "json"
    override val logLevel = PREFERENCE_LOG_LEVEL_IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            IMPORTANT_FOH_ORDERING,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:2 with waitstaff 1.",
        )
    }
}
