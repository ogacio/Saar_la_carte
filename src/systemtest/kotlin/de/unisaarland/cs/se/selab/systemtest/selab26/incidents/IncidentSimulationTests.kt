package de.unisaarland.cs.se.selab.systemtest.selab26.incidents

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG = "DEBUG"
private const val TEN_SEATS = "incidents/restaurants_ten_seats.json"
private const val PREPARATION_PREFIX = "[IMPORTANT] Preparation"
private const val INCIDENT_PREFIX = "[IMPORTANT] Incident"
private const val EVENING_1_PREPARED = "[IMPORTANT] Preparation: Preparation for evening 1 starts."
private const val EVENING_2_PREPARED = "[IMPORTANT] Preparation: Preparation for evening 2 starts."
private const val EVENING_3_PREPARED = "[IMPORTANT] Preparation: Preparation for evening 3 starts."
private const val THREE_EVENINGS = 72

/** The supplier line of the one ingredient these scenarios use. */
private fun procured(grams: Int) = "[DEBUG] Pantry (R 1): Procured $grams g of rice from the supplier."

/** The line of the preparation throwing out yesterday's stock of that ingredient. */
private fun removed(grams: Int) = "[DEBUG] Pantry (R 1): Removed $grams g of rice from the pantry."

/**
 * F32: a RECIPE incident changes how much of an ingredient every recipe needs, so the supplier buys
 * less of it from the evening of the incident onwards - and keeps doing so afterwards.
 */
class F32RecipeIncidentChangesTheProcuredAmount : LogSkippingSystemTest() {
    override val name = "F32RecipeIncidentChangesTheProcuredAmount"
    override val description = "A -50 adaptation on rice turns the 10 g of evening 1 into 5 g from evening 2 on."
    override val food = "incidents/food_rice_ten.json"
    override val restaurants = TEN_SEATS
    override val scenario = "incidents/scenario_recipe_change.json"
    override val logLevel = DEBUG
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, EVENING_1_PREPARED)
        assertNextLine(procured(10))
        skipToAndAssert(
            INCIDENT_PREFIX,
            "[IMPORTANT] Incident: Incident 1 of type RECIPE occurred before evening 2.",
        )
        assertNextLine(EVENING_2_PREPARED)
        // yesterday's stock expired overnight, so the whole amount is bought again - at the new size
        assertNextLine(removed(10))
        assertNextLine(procured(5))
        skipToAndAssert(PREPARATION_PREFIX, EVENING_3_PREPARED)
        assertNextLine(removed(5))
        assertNextLine(procured(5))
    }
}

/**
 * F33: a PACKAGING incident changes the package size of an ingredient, so the supplier still covers
 * the same demand but delivers a different total from the evening of the incident onwards.
 */
class F33PackagingIncidentChangesTheProcuredPackages : LogSkippingSystemTest() {
    override val name = "F33PackagingIncidentChangesTheProcuredPackages"
    override val description = "10 g of rice are one 20 g package in evening 1 and two 5 g packages from evening 2."
    override val food = "incidents/food_rice_packaged.json"
    override val restaurants = TEN_SEATS
    override val scenario = "incidents/scenario_packaging_change.json"
    override val logLevel = DEBUG
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(PREPARATION_PREFIX, EVENING_1_PREPARED)
        assertNextLine(procured(20))
        skipToAndAssert(
            INCIDENT_PREFIX,
            "[IMPORTANT] Incident: Incident 1 of type PACKAGING occurred before evening 2.",
        )
        assertNextLine(EVENING_2_PREPARED)
        assertNextLine(removed(20))
        assertNextLine(procured(10))
        skipToAndAssert(PREPARATION_PREFIX, EVENING_3_PREPARED)
        assertNextLine(removed(10))
        assertNextLine(procured(10))
    }
}

/**
 * F31: a STAFF incident hires a cook mid-simulation. The only dish needs a PASTRY cook the
 * restaurant does not have, so it is off the menu in evening 1 and back on it in evening 2.
 */
class F31StaffIncidentAddsTheCookThatUnlocksTheDish : LogSkippingSystemTest() {
    override val name = "F31StaffIncidentAddsTheCookThatUnlocksTheDish"
    override val description = "Group 1 cannot order Rice Cake in evening 1 and is served it in evening 2."
    override val food = "incidents/food_pastry_only.json"
    override val restaurants = "incidents/restaurants_no_pastry_cook.json"
    override val scenario = "incidents/scenario_staff_change_adds_pastry.json"
    override val logLevel = "INFO"
    override val maxTicks = THREE_EVENINGS

    override suspend fun run() {
        skipToAndAssert(
            "[IMPORTANT] FOH Seating",
            "[IMPORTANT] FOH Seating (R 1): Group 1 seated at table 1 by waitstaff 1.",
        )
        assertNextLine(
            "[IMPORTANT] FOH No Ordering (R 1): Group 1 could not place an order for 2 customers, " +
                "they leave the restaurant.",
        )
        skipToAndAssert(
            INCIDENT_PREFIX,
            "[IMPORTANT] Incident: Incident 1 of type STAFF occurred before evening 2.",
        )
        skipToAndAssert(
            "[IMPORTANT] FOH Ordering",
            "[IMPORTANT] FOH Ordering (R 1): Group 1 placed order 1 of Rice Cake:2 with waitstaff 1.",
        )
        assertNextLine(
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 1 of type PASTRY starts cooking 2 meals " +
                "of dish Rice Cake based on order 1 for orders 1.",
        )
        assertStatistics(1, cooked = 2, served = 2, delivered = 0, ratings = 2)
    }
}
