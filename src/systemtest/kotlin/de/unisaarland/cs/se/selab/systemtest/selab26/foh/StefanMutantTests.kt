package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val INFO = "INFO"
private const val IMPORTANT = "IMPORTANT"
private const val ORDERING_PREFIX = "[IMPORTANT] FOH Ordering"
private const val NO_RESERVING_PREFIX = "[IMPORTANT] FOH No Reserving"
private const val FOH_STEFAN_MUTANTS = "foh/stefan_mutants"
private const val THREE_DISHES_FOOD = "$FOH_STEFAN_MUTANTS/food_three_dishes.json"
private const val ONE_ASIAN_RESTAURANTS = "$FOH_STEFAN_MUTANTS/restaurants_one_asian.json"
private const val ASIAN_ONLY_FOOD = "$FOH_STEFAN_MUTANTS/food_asian_only.json"
private const val COMMON_ONLY_RESTAURANTS = "$FOH_STEFAN_MUTANTS/restaurants_common_only.json"
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"

/** The line that starts [tick] of [evening]. */
private fun tick(tick: Int, evening: Int = 1) = "[IMPORTANT] Simulation: Tick $tick ($evening) started."

/** The line that starts the preparation of [evening]. */
private fun preparation(evening: Int) = "[IMPORTANT] Preparation: Preparation for evening $evening starts."

/**
 * A preference of size 2 applies to both customers: neither orders the chicken dish, both order
 * Egg Rice. If the size were ignored, the second customer would take the highest recipe id.
 */
class F26DinnerForOnePreferenceSizeCoversEveryMember : LogSkippingSystemTest() {
    override val name = "F26DinnerForOnePreferenceSizeCoversEveryMember"
    override val description = "Both members of a size-2 preference avoid chicken and order Egg Rice."
    override val food = "$FOH_STEFAN_MUTANTS/food_egg_or_chicken.json"
    override val restaurants = "$FOH_STEFAN_MUTANTS/restaurants_two_dishes.json"
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_pair_excluding_chicken.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:2 with waitstaff 1.",
        )
    }
}

/**
 * A group of three: one favours Egg Rice, one Chicken Rice, one has no preference (highest id).
 * Three meals are ordered, cooked, served and eaten, and the group rates once.
 */
class F22DinnerForOneEveryCustomerChoosesAndEatsAlone : LogSkippingSystemTest() {
    override val name = "F22DinnerForOneEveryCustomerChoosesAndEatsAlone"
    override val description = "Three customers order individually; statistics count customers, not groups."
    override val food = "$FOH_STEFAN_MUTANTS/food_egg_or_chicken.json"
    override val restaurants = "$FOH_STEFAN_MUTANTS/restaurants_two_dishes.json"
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_three_different_customers.json"
    override val logLevel = INFO
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Chicken Rice:2,Egg Rice:1 with waitstaff 1.",
        )
        skipToAndAssert(
            "[INFO] FOH Finished Eating",
            "[INFO] FOH Finished Eating (R 1): 3 customers of group 1 have finished eating at table 2.",
        )
        assertStatistics(1, cooked = 3, served = 3, delivered = 0, ratings = 1)
    }
}

/** Preferring egg makes Egg Rice (one preferred ingredient) win over Plain Rice (none). */
class F26KingOfTheHillMostPreferredIngredientsBeatHighestId : LogSkippingSystemTest() {
    override val name = "F26KingOfTheHillMostPreferredIngredientsBeatHighestId"
    override val description = "The dish with the most preferred ingredients wins over the highest id."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_kingOfTheHillMostPreferredIngredientsBeatHighestId.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Egg Rice:2 with waitstaff 1.",
        )
    }
}

/** Forum 225: favourites are tried in the listed order, so the one listed first wins. */
class F26KingOfTheHillFirstFavouriteInListOrder : LogSkippingSystemTest() {
    override val name = "F26KingOfTheHillFirstFavouriteInListOrder"
    override val description = "The first matching favourite in list order is chosen, not the lowest or highest id."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_kingOfTheHillFirstFavouriteInListOrder.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Chicken Rice:2 with waitstaff 1.",
        )
    }
}

/** Exclusions come before favourites: a customer who favours but excludes an ingredient skips it. */
class F26KingOfTheHillExcludedIngredientBeatsFavourite : LogSkippingSystemTest() {
    override val name = "F26KingOfTheHillExcludedIngredientBeatsFavourite"
    override val description = "An excluded ingredient rules out even a favourite dish."
    override val food = THREE_DISHES_FOOD
    override val restaurants = ONE_ASIAN_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_kingOfTheHillExcludedIngredientBeatsFavourite.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            ORDERING_PREFIX,
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Plain Rice:2 with waitstaff 1.",
        )
    }
}

/**
 * Forum 335 (staff): the ordering tick is waiting tick 1. The only cook is busy with group 1's
 * stew, so group 2's soup cannot be served before the deadline: group 2 leaves in ordered tick + 4
 * and rates NEGATIVE.
 */
class F27PatienceUnservedGroupLeavesInTheFifthWaitingTick : LogSkippingSystemTest() {
    override val name = "F27PatienceUnservedGroupLeavesInTheFifthWaitingTick"
    override val description = "Group 2, ordering in tick 3, leaves unserved in tick 7 and rates negative."
    override val food = "$FOH_STEFAN_MUTANTS/food_slow.json"
    override val restaurants = "$FOH_STEFAN_MUTANTS/restaurants_two_pairs.json"
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_second_group_waits_too_long.json"
    override val logLevel = INFO
    override val maxTicks = 12

    override suspend fun run() {
        skipToAndAssert(tick(7), tick(7))
        assertNextLine(
            "[INFO] Restaurant No Eating (R 1): 2 customers of group 2 leave table 2 due to not being served.",
        )
        skipToPrefix("[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating")
    }
}

/** Forum 302: a REGULAR group whose reservation fails rates NEGATIVE in the rating step of tick 1. */
class P05BacklashFailedReservationRatesNegativeInTickOne : LogSkippingSystemTest() {
    override val name = "P05BacklashFailedReservationRatesNegativeInTickOne"
    override val description = "A regular without a reservation rates the restaurant negative in tick 1."
    override val food = ASIAN_ONLY_FOOD
    override val restaurants = COMMON_ONLY_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_regular_needs_a_bar_table.json"
    override val logLevel = INFO
    override val maxTicks = 1

    override suspend fun run() {
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}

/**
 * Two events of four for evening 4: group 1 gets the 6-seat table, group 2 finds no table on its
 * event evening and rates NEGATIVE in tick 1 (forum 302).
 */
class P05BacklashFailedEventReservationRatesNegative : LogSkippingSystemTest() {
    override val name = "P05BacklashFailedEventReservationRatesNegative"
    override val description = "The event without a table on its evening rates the restaurant negative in tick 1."
    override val food = ASIAN_ONLY_FOOD
    override val restaurants = "$FOH_STEFAN_MUTANTS/restaurants_event_six_and_two.json"
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_two_events_one_big_table.json"
    override val logLevel = INFO
    override val maxTicks = 73

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] Preparation: Preparation for evening 4", preparation(4))
        skipToAndAssert(
            NO_RESERVING_PREFIX,
            "[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 2.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 2 rates the restaurant 1 with NEGATIVE rating, " +
                "leading to 0 positive ratings and 1 negative ratings.",
        )
    }
}

/**
 * visitingStart 2, visitingPeriod 3: the regular pair visits on evenings 2 and 5 of five, so 4
 * meals are cooked and served and 2 ratings given.
 */
class F22MoeAndBarneyRegularStartTwoPeriodThree : LogSkippingSystemTest() {
    override val name = "F22MoeAndBarneyRegularStartTwoPeriodThree"
    override val description = "A regular with start 2 and period 3 visits exactly on evenings 2 and 5."
    override val food = ASIAN_ONLY_FOOD
    override val restaurants = COMMON_ONLY_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_regular_start_two_period_three.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 120

    override suspend fun run() {
        assertStatistics(1, cooked = 4, served = 4, delivered = 0, ratings = 2)
    }
}

/**
 * A regular that needs a BAR table fails its reservation on evenings 1 and 2; after two
 * consecutive failed attempts it never comes again, so evening 3 has no No Reserving line.
 */
class F22MoeAndBarneyRegularStopsAfterTwoFailedAttempts : LogSkippingSystemTest() {
    override val name = "F22MoeAndBarneyRegularStopsAfterTwoFailedAttempts"
    override val description = "Two failed reservations in a row: the regular is gone on evening 3."
    override val food = ASIAN_ONLY_FOOD
    override val restaurants = COMMON_ONLY_RESTAURANTS
    override val scenario = "$FOH_STEFAN_MUTANTS/scenario_regular_fails_twice.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 72

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, preparation(1))
        assertNextLine("[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 1.")
        skipToAndAssert(PREPARATION_PREFIX, preparation(2))
        assertNextLine("[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 1.")
        skipToAndAssert(PREPARATION_PREFIX, preparation(3))
        assertNextLine("[IMPORTANT] Serving: Serving of evening 3 starts.")
    }
}
