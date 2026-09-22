package de.unisaarland.cs.se.selab.systemtest.selab26.kitchen

import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val DEBUG_LEVEL_F09 = "DEBUG"
private const val EMPTY_SCENARIO_F09 = "f09/scenario_empty.json"
private const val PREPARATION_F09 = "[IMPORTANT] Preparation"
private const val PANTRY_F09 = "[DEBUG] Pantry"
private const val EVENING_1_F09 = "[IMPORTANT] Preparation: Preparation for evening 1 starts."
private const val EVENING_4_F09 = "[IMPORTANT] Preparation: Preparation for evening 4 starts."
private const val RICE_F09 = "rice"
private const val APPLE_F09 = "apple"
private const val TRUFFLE_F09 = "truffle"
private const val BOUNDARY_FOOD = "f09/food_planning_boundaries.json"
private const val FOURTEEN_SEATS = "f09/restaurants_fourteen_seats.json"

/** F09 additions that isolate seat estimates, reservations, events and regular history. */
abstract class AddedPlanningTest(
    foodFixture: String,
    restaurantFixture: String,
    scenarioFixture: String = EMPTY_SCENARIO_F09,
    ticks: Int = 1,
) : LogSkippingSystemTest() {
    override val food = foodFixture
    override val restaurants = restaurantFixture
    override val scenario = scenarioFixture
    override val logLevel = DEBUG_LEVEL_F09
    override val maxTicks = ticks

    protected fun procured(amount: Int, ingredient: String): String =
        "[DEBUG] Pantry (R 1): Procured $amount g of $ingredient from the supplier."

    protected fun removed(amount: Int, ingredient: String): String =
        "[DEBUG] Pantry (R 1): Removed $amount g of $ingredient from the pantry."
}

/** Ten free seats produce one estimated order per menu dish. */
class F09TenSeatsEstimateOne : AddedPlanningTest(
    BOUNDARY_FOOD,
    "f09/restaurants_ten_seats.json",
) {
    override val name = "F09TenSeatsEstimateOne"
    override val description = "Ten free seats produce one estimated portion."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_F09, EVENING_1_F09)
        skipToAndAssert(PANTRY_F09, procured(10, RICE_F09))
    }
}

/** Eleven free seats round up to two estimated orders per menu dish. */
class F09ElevenSeatsEstimateTwo : AddedPlanningTest(
    BOUNDARY_FOOD,
    "f09/restaurants_eleven_seats.json",
) {
    override val name = "F09ElevenSeatsEstimateTwo"
    override val description = "Eleven free seats produce two estimated portions."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_F09, EVENING_1_F09)
        skipToAndAssert(PANTRY_F09, procured(20, RICE_F09))
    }
}

/** The seat estimate is applied independently to every dish on the menu. */
class F09EveryMenuDishIsPlanned : AddedPlanningTest(
    "f09/food_two_menu_dishes.json",
    "f09/restaurants_eleven_seats_two_dishes.json",
) {
    override val name = "F09EveryMenuDishIsPlanned"
    override val description = "Eleven seats produce two portions of each menu dish."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_F09, EVENING_1_F09)
        skipToAndAssert(PANTRY_F09, procured(20, APPLE_F09))
        assertNextLine(procured(20, "zucchini"))
    }
}

/** A successful reservation removes its table capacity from the casual-seat estimate. */
class F09ReservedTableCapacityReducesOtherSeats : AddedPlanningTest(
    BOUNDARY_FOOD,
    FOURTEEN_SEATS,
    "f09/scenario_regular_size_three.json",
) {
    override val name = "F09ReservedTableCapacityReducesOtherSeats"
    override val description = "A size-three regular leaves eleven seats for estimation."

    override suspend fun run() {
        skipToAndAssert(PREPARATION_F09, EVENING_1_F09)
        skipToAndAssert(PANTRY_F09, procured(20, RICE_F09))
    }
}

/** A group whose reservation fails must not reduce the free-seat estimate. */
class F09FailedReservationDoesNotReduceOtherSeats : AddedPlanningTest(
    "f09/food_event_favourite.json",
    "f09/restaurants_event_fourteen_seats.json",
    "f09/scenario_failed_event_reservation.json",
    73,
) {
    override val name = "F09FailedReservationDoesNotReduceOtherSeats"
    override val description = "A failed event reservation is excluded and leaves all seats for estimation."

    override suspend fun run() {
        skipToAndAssert(EVENING_4_F09, EVENING_4_F09)
        skipToAndAssert(PANTRY_F09, removed(20, TRUFFLE_F09))
        assertNextLine(procured(20, TRUFFLE_F09))
    }
}

/** An event plans one favourite dish for every member of the group. */
class F09EventFavouritePlanning : AddedPlanningTest(
    "f09/food_event_favourite.json",
    "f09/restaurants_event_four_seats.json",
    "f09/scenario_event_size_four.json",
    73,
) {
    override val name = "F09EventFavouritePlanning"
    override val description = "A four-person event procures four portions of its ASIAN favourite."

    override suspend fun run() {
        skipToAndAssert(EVENING_4_F09, EVENING_4_F09)
        skipToAndAssert(PANTRY_F09, removed(10, TRUFFLE_F09))
        assertNextLine(procured(40, TRUFFLE_F09))
    }
}

/** The fourth preparation plans the dishes ordered during the preceding three regular visits. */
class F09KnownRegularUsesLastThreeVisits : AddedPlanningTest(
    "f09/food_regular_history.json",
    "f09/restaurants_regular_history.json",
    "f09/scenario_regular_history.json",
    73,
) {
    override val name = "F09KnownRegularUsesLastThreeVisits"
    override val description = "Preparation four includes all dishes from the regular's last three visits."

    override suspend fun run() {
        skipToAndAssert(EVENING_4_F09, EVENING_4_F09)
        skipToAndAssert(PANTRY_F09, removed(20, APPLE_F09))
        assertNextLine(procured(60, APPLE_F09))
        assertNextLine(procured(40, "beet"))
    }
}
