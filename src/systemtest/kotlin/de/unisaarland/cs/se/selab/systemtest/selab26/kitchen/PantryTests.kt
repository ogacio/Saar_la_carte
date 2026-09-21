package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

/* F08, F09 and P01: what the kitchen buys before the doors open, and what it throws away. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL = "DEBUG"
private const val PREPARATION = "[IMPORTANT] Preparation"
private const val PANTRY = "[DEBUG] Pantry"
private const val TWENTY_SEATS = "f09/restaurants_twenty_seats.json"
private const val NO_CUSTOMERS = "f09/scenario_empty.json"
private const val EVENING_1 = "[IMPORTANT] Preparation: Preparation for evening 1 starts."
private const val EVENING_2 = "[IMPORTANT] Preparation: Preparation for evening 2 starts."
private const val RESTOCKED = "[INFO] Pantry (R 1): Restocked ingredients."
private const val PROCURED_100 = "[DEBUG] Pantry (R 1): Procured 100 g of rice from the supplier."

/** F08: ingredients come in whole packages, so 50 g of a 20 g ingredient costs three of them. */
class F08SupplierDeliversWholePackages : LogSkippingSystemTest() {
    override val name = "F08SupplierDeliversWholePackages"
    override val description = "50 g of a 20 g ingredient are bought as three packages of 20 g."
    override val food = "f08/food_rice_packs.json"
    override val restaurants = "f08/restaurants_ten_seats.json"
    override val scenario = "f08/scenario_empty.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 1

    override suspend fun run() {
        skipToAndAssert(PREPARATION, EVENING_1)
        skipToAndAssert(PANTRY, "[DEBUG] Pantry (R 1): Procured 60 g of rice from the supplier.")
        assertNextLine(RESTOCKED)
    }
}

/** F09: the kitchen plans one order per dish per ten seats, so twenty seats are two portions. */
class F09PlanningEstimatesOneGroupPerTenSeats : LogSkippingSystemTest() {
    override val name = "F09PlanningEstimatesOneGroupPerTenSeats"
    override val description = "Twenty seats are two expected groups, so two portions are bought."
    override val food = "f09/food_rice_one_evening.json"
    override val restaurants = TWENTY_SEATS
    override val scenario = NO_CUSTOMERS
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 1

    override suspend fun run() {
        skipToAndAssert(PREPARATION, EVENING_1)
        skipToAndAssert(PANTRY, PROCURED_100)
    }
}

/** P01: bestBefore counts the day of purchase, so one day is gone by the next evening. */
class P01IngredientsLastTheirBestBeforeDays : LogSkippingSystemTest() {
    override val name = "P01IngredientsLastTheirBestBeforeDays"
    override val description = "Rice with bestBefore 1 is thrown out in the preparation of the next evening."
    override val food = "f09/food_rice_one_evening.json"
    override val restaurants = TWENTY_SEATS
    override val scenario = NO_CUSTOMERS
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 25

    override suspend fun run() {
        skipToAndAssert(PREPARATION, EVENING_1)
        skipToAndAssert(PANTRY, PROCURED_100)
        skipToAndAssert(PREPARATION, EVENING_2)
        skipToAndAssert(PANTRY, "[DEBUG] Pantry (R 1): Removed 100 g of rice from the pantry.")
        assertNextLine(PROCURED_100)
    }
}

/** F08: only ingredients of which there is not enough left in the pantry are bought again. */
class F08TheSupplierBuysOnlyWhatIsMissing : LogSkippingSystemTest() {
    override val name = "F08TheSupplierBuysOnlyWhatIsMissing"
    override val description = "With last evening's stock still in the pantry, nothing is procured again."
    override val food = "f08/food_rice_packs.json"
    override val restaurants = "f08/restaurants_ten_seats.json"
    override val scenario = "f08/scenario_empty.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 25

    override suspend fun run() {
        skipToAndAssert(PREPARATION, EVENING_1)
        skipToAndAssert(PANTRY, "[DEBUG] Pantry (R 1): Procured 60 g of rice from the supplier.")
        skipToAndAssert(PREPARATION, EVENING_2)
        assertNextLine(RESTOCKED)
    }
}

/** P01: two days are the evening of purchase and the one after, so evening three throws it out. */
class P01BestBeforeTwoLastsExactlyTwoEvenings : LogSkippingSystemTest() {
    override val name = "P01BestBeforeTwoLastsExactlyTwoEvenings"
    override val description = "Rice with bestBefore 2 survives evening 2 and is thrown out in evening 3."
    override val food = "f09/food_rice_two_evenings.json"
    override val restaurants = TWENTY_SEATS
    override val scenario = NO_CUSTOMERS
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 50

    override suspend fun run() {
        skipToAndAssert(PREPARATION, EVENING_1)
        skipToAndAssert(PANTRY, PROCURED_100)
        skipToAndAssert(PREPARATION, EVENING_2)
        assertNextLine(RESTOCKED)
        skipToAndAssert(PREPARATION, "[IMPORTANT] Preparation: Preparation for evening 3 starts.")
        assertNextLine("[DEBUG] Pantry (R 1): Removed 100 g of rice from the pantry.")
        assertNextLine(PROCURED_100)
    }
}
