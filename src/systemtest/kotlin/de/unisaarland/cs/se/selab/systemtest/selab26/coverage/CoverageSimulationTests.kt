package de.unisaarland.cs.se.selab.systemtest.selab26.coverage

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val INFO = "INFO"
private const val IMPORTANT = "IMPORTANT"
private const val FOOD_RICE_BOWL = "foh/food_rice.json"
private const val FOOD_RICE_TEN = "incidents/food_rice_ten.json"
private const val TEN_SEATS = "incidents/restaurants_ten_seats.json"
private const val PREPARATION = "[IMPORTANT] Preparation"
private const val RESTOCKED = "[INFO] Pantry (R 1): Restocked ingredients."
private const val PROCURED_RICE = "[DEBUG] Pantry (R 1): Procured 10 g of rice from the supplier."
private const val TWO_EVENINGS = 48
private const val GROUP_1_DECIDED = "[DEBUG] Restaurant Decision: Group 1 decided on restaurant 1."

/** The line that starts [tick] of [evening]. */
private fun tickStarted(tick: Int, evening: Int) = "[IMPORTANT] Simulation: Tick $tick ($evening) started."

/** The line that starts the preparation of [evening]. */
private fun preparation(evening: Int) = "[IMPORTANT] Preparation: Preparation for evening $evening starts."

/**
 * F34: an ingredient that is UNAVAILABLE for two evenings is not procured in either of them (the
 * incident evening counts), and is bought again on the evening after. The Restocked line is written
 * every preparation, even when nothing was bought (forum thread 314).
 */
class F34UnavailableIngredientIsNotProcuredForItsDuration : LogSkippingSystemTest() {
    override val name = "F34UnavailableIngredientIsNotProcuredForItsDuration"
    override val description = "Rice unavailable on evenings 2 and 3: nothing procured, 10 g again on evening 4."
    override val food = FOOD_RICE_TEN
    override val restaurants = TEN_SEATS
    override val scenario = "coverage/scenario_rice_unavailable_two_evenings.json"
    override val logLevel = DEBUG
    override val maxTicks = 73

    override suspend fun run() {
        skipToAndAssert(PREPARATION, preparation(1))
        assertNextLine(PROCURED_RICE)
        assertNextLine(RESTOCKED)
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type UNAVAILABLE occurred before evening 2.",
        )
        assertNextLine(preparation(2))
        assertNextLine("[DEBUG] Pantry (R 1): Removed 10 g of rice from the pantry.")
        assertNextLine(RESTOCKED)
        skipToAndAssert(PREPARATION, preparation(3))
        assertNextLine(RESTOCKED)
        skipToAndAssert(PREPARATION, preparation(4))
        assertNextLine(PROCURED_RICE)
        assertNextLine(RESTOCKED)
    }
}

/**
 * F31 + F28: a restaurant without drivers is no option for a delivery group; once a STAFF incident
 * hires a DRIVER, the same group decides on it. Distance 5 and visitingTick 6 put the decision into
 * tick 6 - 1 - 3 = 2 (specification adjustment #10).
 */
class F31HiredDriverMakesTheRestaurantAvailableForDelivery : LogSkippingSystemTest() {
    override val name = "F31HiredDriverMakesTheRestaurantAvailableForDelivery"
    override val description = "No driver on evening 1: no decision; after STAFF DRIVER +1 the group decides."
    override val food = FOOD_RICE_TEN
    override val restaurants = TEN_SEATS
    override val scenario = "coverage/scenario_driver_hired_for_delivery.json"
    override val logLevel = DEBUG
    override val maxTicks = TWO_EVENINGS

    override suspend fun run() {
        skipToAndAssert(tickStarted(2, 1), tickStarted(2, 1))
        assertNextLine("[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant.")
        skipToAndAssert(
            "[IMPORTANT] Incident",
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 2.",
        )
        skipToAndAssert(tickStarted(2, 2), tickStarted(2, 2))
        assertNextLine(GROUP_1_DECIDED)
    }
}

/**
 * F20 + F29: a delivery of two meals from ordering to the rating. The order line has no waitstaff part,
 * a waiter hands both meals to driver 1, the driver prepares, arrives and hands them over, returns,
 * the group eats and rates. Delivered customers are not counted as served (forum thread 292).
 */
class F20DeliveryOfTwoMealsFromOrderToRating : LogSkippingSystemTest() {
    override val name = "F20DeliveryOfTwoMealsFromOrderToRating"
    override val description = "Order, hand-over to driver 1, delivery, return, eating, rating and statistics."
    override val food = FOOD_RICE_BOWL
    override val restaurants = "coverage/restaurants_one_driver.json"
    override val scenario = "coverage/scenario_delivery_of_two.json"
    override val logLevel = INFO
    override val maxTicks = 24

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Bowl:2.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Delivery",
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 1 serves Rice Bowl:2 meals to driver 1 for order 1.",
        )
        skipToAndAssert(
            "[INFO] Delivery Preparation",
            "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 1 to group 1, which will take 1 ticks.",
        )
        skipToAndAssert(
            "[INFO] Delivery Arrival",
            "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 1 with order 1.",
        )
        assertNextLine("[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 1 to group 1.")
        skipToAndAssert("[INFO] Delivery Returned", "[INFO] Delivery Returned (R 1): Driver 1 has returned.")
        skipToAndAssert(
            "[INFO] Delivery Finished Eating",
            "[INFO] Delivery Finished Eating (R 1): Group 1 has finished eating.",
        )
        skipToAndAssert(
            "[INFO] Rating",
            "[INFO] Rating (R 1): Group 1 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 1 positive ratings and 0 negative ratings.",
        )
        assertStatistics(1, cooked = 2, served = 0, delivered = 2, ratings = 1)
    }
}

/**
 * F22: a REGULAR group with visitingStart 1 and visitingPeriod 2 visits on evenings 1 and 3 only (not
 * 2), and rates every visit. Over three evenings: 4 meals cooked, 4 customers served, 2 ratings.
 */
class F22RegularVisitsEverySecondEvening : LogSkippingSystemTest() {
    override val name = "F22RegularVisitsEverySecondEvening"
    override val description = "Regular pair, start 1, period 2, three evenings: two visits, four meals, two ratings."
    override val food = FOOD_RICE_BOWL
    override val restaurants = "coverage/restaurants_two_small_tables.json"
    override val scenario = "coverage/scenario_regular_every_second_evening.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 72

    override suspend fun run() {
        assertStatistics(1, cooked = 4, served = 4, delivered = 0, ratings = 2)
    }
}

/**
 * F28 (browsing capacity): once group 1 decided on the only 2-seat table, the service counts those
 * seats as taken, so group 2 deciding in the same tick finds no restaurant.
 */
class F28DecidedGroupTakesTheLastSeats : LogSkippingSystemTest() {
    override val name = "F28DecidedGroupTakesTheLastSeats"
    override val description = "Two pairs decide in tick 3 for one 2-seat table: group 1 decides, group 2 cannot."
    override val food = FOOD_RICE_BOWL
    override val restaurants = "coverage/restaurants_one_table_of_two.json"
    override val scenario = "coverage/scenario_two_pairs_same_tick.json"
    override val logLevel = DEBUG
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(tickStarted(3, 1), tickStarted(3, 1))
        assertNextLine(GROUP_1_DECIDED)
        assertNextLine("[DEBUG] Restaurant No Decision: Group 2 could not decide for a restaurant.")
    }
}

/**
 * F25: a group only considers restaurants of its own types. The only restaurant is AFRICAN, so the
 * ASIAN group 1 finds nothing while the AFRICAN group 2 decides on it.
 */
class F25OnlyRestaurantsOfTheWantedTypeAreConsidered : LogSkippingSystemTest() {
    override val name = "F25OnlyRestaurantsOfTheWantedTypeAreConsidered"
    override val description = "AFRICAN restaurant: the ASIAN group finds none, the AFRICAN group decides on it."
    override val food = "coverage/food_african_rice.json"
    override val restaurants = "coverage/restaurants_african.json"
    override val scenario = "coverage/scenario_asian_and_african_groups.json"
    override val logLevel = DEBUG
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(tickStarted(3, 1), tickStarted(3, 1))
        assertNextLine("[DEBUG] Restaurant No Decision: Group 1 could not decide for a restaurant.")
        assertNextLine("[DEBUG] Restaurant Decision: Group 2 decided on restaurant 1.")
    }
}

/**
 * F26: customers order one by one, the one with the most excluded ingredients first. Only one portion
 * of Egg Rice is in stock. The customer who excludes chicken can only eat Egg Rice and orders first,
 * so the customer who merely favours Egg Rice (listed first in the JSON) falls back to the highest
 * remaining recipe, Chicken Rice. In the wrong order, one customer could not order at all.
 */
class F26CustomerWithMoreExclusionsOrdersFirst : LogSkippingSystemTest() {
    override val name = "F26CustomerWithMoreExclusionsOrdersFirst"
    override val description = "The chicken-excluding customer gets the only Egg Rice, the other one Chicken Rice."
    override val food = "coverage/food_chicken_or_egg.json"
    override val restaurants = "coverage/restaurants_one_table_two_recipes.json"
    override val scenario = "coverage/scenario_scarce_dish_goes_to_the_pickier_customer.json"
    override val logLevel = IMPORTANT
    override val maxTicks = 3

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Chicken Rice:1,Egg Rice:1 with waitstaff 1.",
        )
    }
}

/**
 * P04: an EVENT group decides in the first tick of the evening three evenings before its event
 * (forum threads 321 and adjustment #14), the restaurant reserves its table on the event evening, and
 * the group arrives and is seated at its visitingTick there.
 */
class P04EventDecidesThreeEveningsAheadAndArrivesOnItsEvening : LogSkippingSystemTest() {
    override val name = "P04EventDecidesThreeEveningsAheadAndArrivesOnItsEvening"
    override val description = "Event for evening 4 decides in tick 1 of evening 1, is seated in tick 2 of evening 4."
    override val food = FOOD_RICE_BOWL
    override val restaurants = "coverage/restaurants_event_table_of_four.json"
    override val scenario = "coverage/scenario_event_on_evening_four.json"
    override val logLevel = DEBUG
    override val maxTicks = 74

    override suspend fun run() {
        skipToAndAssert(tickStarted(1, 1), tickStarted(1, 1))
        assertNextLine(GROUP_1_DECIDED)
        skipToAndAssert(PREPARATION + ": Preparation for evening 4", preparation(4))
        skipToAndAssert(tickStarted(2, 4), tickStarted(2, 4))
        skipToAndAssert(
            "[INFO] Restaurant Arrival",
            "[INFO] Restaurant Arrival (R 1): Group 1 arrived at restaurant 1.",
        )
        assertNextLine("[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.")
    }
}
