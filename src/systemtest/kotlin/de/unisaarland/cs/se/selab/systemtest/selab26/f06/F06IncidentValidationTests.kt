package de.unisaarland.cs.se.selab.systemtest.selab26.f06

/* F06: the scenario file is rejected when an incident cannot be applied. */

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val VALID_FOOD = "f03/root/valid/ok_baseline.json"
private const val VALID_RESTAURANTS = "f03/companions/restaurants_valid.json"
private const val DEBUG_LEVEL = "DEBUG"
private const val INVALID = "[IMPORTANT] Initialization Info"

/** Every incident of this file can be applied, so the scenario is accepted. */
class F06IncidentsValid : LogSkippingSystemTest() {
    override val name = "F06IncidentsValid"
    override val description = "A staff change and an ingredient unavailability that both resolve are accepted."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_valid.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        assertNextLine("[INFO] Initialization Info: ok_baseline.json successfully parsed and validated.")
        assertNextLine("[INFO] Initialization Info: restaurants_valid.json successfully parsed and validated.")
        assertNextLine(
            "[INFO] Initialization Info: scenario_incident_valid.json successfully parsed and validated.",
        )
    }
}

/** "It also contains a restaurant property that contains the id of an existing restaurant." */
class F06IncidentUnknownRestaurant : LogSkippingSystemTest() {
    override val name = "F06IncidentUnknownRestaurant"
    override val description = "A staff change for a restaurant that does not exist makes the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_unknown_restaurant.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_unknown_restaurant.json is invalid.",
        )
    }
}

/** An unavailability can only be declared for an ingredient the food file knows. */
class F06IncidentUnknownIngredient : LogSkippingSystemTest() {
    override val name = "F06IncidentUnknownIngredient"
    override val description = "An unavailability of an ingredient that does not exist makes the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_unknown_ingredient.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_unknown_ingredient.json is invalid.",
        )
    }
}

/** "Each incident has a unique id." */
class F06IncidentDuplicateId : LogSkippingSystemTest() {
    override val name = "F06IncidentDuplicateId"
    override val description = "Two incidents with the same id make the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_duplicate_id.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_duplicate_id.json is invalid.",
        )
    }
}

/**
 * "The duration of ingredient unavailable incidents must not overlap for the same ingredient"
 * (forum update 7): evenings 2 to 4 and 3 to 4 overlap for rice.
 */
class F06OverlappingUnavailability : LogSkippingSystemTest() {
    override val name = "F06OverlappingUnavailability"
    override val description = "Two unavailabilities of one ingredient that overlap make the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_overlapping_unavailability.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_overlapping_unavailability.json is invalid.",
        )
    }
}

/** "The incident has a non-zero number property", so a staff change without one is incomplete. */
class F06StaffChangeWithoutNumber : LogSkippingSystemTest() {
    override val name = "F06StaffChangeWithoutNumber"
    override val description = "A staff change without a number makes the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_staff_without_number.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_staff_without_number.json is invalid.",
        )
    }
}

/** "This can be any cook type but EXEC", since a restaurant has at most one head cook. */
class F06StaffChangeOfTheExecCook : LogSkippingSystemTest() {
    override val name = "F06StaffChangeOfTheExecCook"
    override val description = "A staff change of the EXEC cook makes the scenario invalid."
    override val food = VALID_FOOD
    override val restaurants = VALID_RESTAURANTS
    override val scenario = "f06/scenario_incident_exec_cook_change.json"
    override val logLevel = DEBUG_LEVEL
    override val maxTicks = 0
    override suspend fun run() {
        skipToAndAssert(
            INVALID,
            "[IMPORTANT] Initialization Info: scenario_incident_exec_cook_change.json is invalid.",
        )
    }
}
