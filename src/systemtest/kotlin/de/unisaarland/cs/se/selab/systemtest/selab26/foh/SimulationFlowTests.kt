package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val F01_RESTAURANTS = "foh/f01/restaurants.json"
private const val EMPTY_SCENARIO = "foh/f01/scenario.json"
private const val FOOD_RICE = "foh/food_rice.json"
private const val IMPORTANT = "IMPORTANT"
private const val EVENING_1_ENDS = "[IMPORTANT] Serving: Serving of evening 1 ends."
private const val STATISTICS_CALCULATED = "[IMPORTANT] Simulation Info: Simulation statistics are calculated."

/** F01: the first tick of a run with one restaurant and no customers, logged at INFO. */
class F01FirstTickLogOrder : LogSkippingSystemTest() {
    override val name = "F01FirstTickLogOrder"
    override val description = "Initialization, preparation, serving start and one tick in spec order."
    override val food = FOOD_RICE
    override val restaurants = F01_RESTAURANTS
    override val scenario = EMPTY_SCENARIO
    override val logLevel = "INFO"
    override val maxTicks = 1

    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: food_rice.json successfully parsed and validated.")
        assertNextLine("[INFO] Initialization Info: restaurants.json successfully parsed and validated.")
        assertNextLine("[INFO] Initialization Info: scenario.json successfully parsed and validated.")
        assertNextLine("[INFO] Simulation Info: Simulation started.")
        assertNextLine("[IMPORTANT] Preparation: Preparation for evening 1 starts.")
        assertNextLine("[INFO] Pantry (R 1): Restocked ingredients.")
        assertNextLine("[IMPORTANT] Serving: Serving of evening 1 starts.")
        assertNextLine("[IMPORTANT] Simulation: Tick 1 (1) started.")
        assertNextLine(STATISTICS_CALCULATED)
        assertStatistics(1, cooked = 0, served = 0, delivered = 0, ratings = 0)
        assertEnd()
    }
}

/** F01: a run of exactly 24 ticks ends with the serving phase and never prepares evening 2. */
class F01StopsAfterFullEvening : LogSkippingSystemTest() {
    override val name = "F01StopsAfterFullEvening"
    override val description = "maxTicks 24 ends after 'Serving of evening 1 ends' without a new preparation."
    override val food = FOOD_RICE
    override val restaurants = F01_RESTAURANTS
    override val scenario = EMPTY_SCENARIO
    override val logLevel = IMPORTANT
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] Simulation: Tick 24", "[IMPORTANT] Simulation: Tick 24 (24) started.")
        assertNextLine(EVENING_1_ENDS)
        assertNextLine(STATISTICS_CALCULATED)
    }
}

/** F01: tick 25 is the first tick of evening 2, after the preparation of evening 2. */
class F01SecondEveningStartsAtTick25 : LogSkippingSystemTest() {
    override val name = "F01SecondEveningStartsAtTick25"
    override val description = "Evening 1 ends, evening 2 is prepared and tick 25 is tick 1 of the evening."
    override val food = FOOD_RICE
    override val restaurants = F01_RESTAURANTS
    override val scenario = EMPTY_SCENARIO
    override val logLevel = IMPORTANT
    override val maxTicks = 25

    override suspend fun run() {
        skipToAndAssert("[IMPORTANT] Serving: Serving of evening 1 ends", EVENING_1_ENDS)
        assertNextLine("[IMPORTANT] Preparation: Preparation for evening 2 starts.")
        assertNextLine("[IMPORTANT] Serving: Serving of evening 2 starts.")
        assertNextLine("[IMPORTANT] Simulation: Tick 25 (1) started.")
        assertNextLine(STATISTICS_CALCULATED)
    }
}

/** F07: statistics are reported in ascending restaurant id and ignore the initial ratings. */
class F07StatisticsInAscendingRestaurantId : LogSkippingSystemTest() {
    override val name = "F07StatisticsInAscendingRestaurantId"
    override val description = "Restaurants listed as 2, 1 report as 1, 2 with zero received ratings."
    override val food = "foh/food_two_types.json"
    override val restaurants = "foh/f07/restaurants_reversed.json"
    override val scenario = EMPTY_SCENARIO
    override val logLevel = IMPORTANT
    override val maxTicks = 0

    override suspend fun run() {
        assertStatistics(1, cooked = 0, served = 0, delivered = 0, ratings = 0)
        assertStatistics(2, cooked = 0, served = 0, delivered = 0, ratings = 0)
        assertEnd()
    }
}
