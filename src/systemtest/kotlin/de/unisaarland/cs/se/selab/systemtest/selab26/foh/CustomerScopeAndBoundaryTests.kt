package de.unisaarland.cs.se.selab.systemtest.selab26.foh

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

private const val CUSTOMER_BOUNDARY_FOOD = "f12/food_ten_minute.json"
private const val CUSTOMER_BOUNDARY_RESTAURANT = "f12/restaurants_one_recipe.json"
private const val ARRIVAL_FIRST_EVENING = "1/1 arrival 1 1"
private const val ARRIVAL_SECOND_EVENING = "2/1 arrival 1 1"

/** Complete customer traces catch extra visits, repeated decisions and ratings on the wrong tick. */
abstract class AddedCustomerBoundaryTest : LogSkippingSystemTest() {
    override val food = CUSTOMER_BOUNDARY_FOOD
    override val restaurants = CUSTOMER_BOUNDARY_RESTAURANT
    override val logLevel = "DEBUG"

    protected suspend fun assertCustomerTrace(expected: List<String>, decisionsOnly: Boolean = false) {
        val actual = mutableListOf<String>()
        val clock = Regex("Simulation: Tick (\\d+) \\((\\d+)\\)")
        val patterns = linkedMapOf(
            "decision" to Regex("Restaurant Decision: Group (\\d+) decided on restaurant (\\d+)"),
            "none" to Regex("Restaurant No Decision: Group (\\d+)"),
        )
        if (!decisionsOnly) {
            patterns["arrival"] = Regex("Restaurant Arrival \\(R (\\d+)\\): Group (\\d+)")
            patterns["rating"] = Regex(
                "Rating \\(R (\\d+)\\): Group (\\d+) rates the restaurant \\d+ with (\\w+) rating",
            )
        }
        var time = "before ticks"
        while (true) {
            val line = getNextLine() ?: break
            clock.find(line)?.let { time = "${it.groupValues[2]}/${it.groupValues[1]}" }
            for ((label, pattern) in patterns) {
                pattern.find(line)?.let { actual += "$time $label ${it.groupValues.drop(1).joinToString(" ")}" }
            }
        }
        if (actual != expected) throw SystemTestAssertionError("Expected customer trace $expected but got $actual")
    }
}

/** F22: a successful visit clears the failure streak; the regular still comes on evening four. */
class F22SuccessBetweenFailuresPreservesFutureVisits : AddedCustomerBoundaryTest() {
    override val name = "F22SuccessBetweenFailuresPreservesFutureVisits"
    override val description = "Missing waiter, success, missing waiter, success: all four visits are rated once."
    override val scenario = "f22/scenario_success_between_failures.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertCustomerTrace(
            listOf(
                ARRIVAL_FIRST_EVENING,
                "1/2 rating 1 1 NEGATIVE",
                ARRIVAL_SECOND_EVENING,
                "2/3 rating 1 1 POSITIVE",
                "3/1 arrival 1 1",
                "3/2 rating 1 1 NEGATIVE",
                "4/1 arrival 1 1",
                "4/3 rating 1 1 POSITIVE",
            ),
        )
    }
}

/** F22: a failed seating on each of two evenings ends the regular's visits permanently. */
class F22TwoSeatingFailuresStopVisitsAfterStaffReturns : AddedCustomerBoundaryTest() {
    override val name = "F22TwoSeatingFailuresStopVisitsAfterStaffReturns"
    override val description = "Restoring the waiter on evening three cannot bring back a regular that gave up."
    override val scenario = "f22/scenario_two_seating_failures.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertCustomerTrace(
            listOf(ARRIVAL_FIRST_EVENING, "1/2 rating 1 1 NEGATIVE", ARRIVAL_SECOND_EVENING, "2/2 rating 1 1 NEGATIVE"),
        )
    }
}

/** F22: nobody receiving food counts as a failed attempt even though seating succeeded. */
class F22TwoUnservedVisitsStopVisitsAfterFoodReturns : AddedCustomerBoundaryTest() {
    override val name = "F22TwoUnservedVisitsStopVisitsAfterFoodReturns"
    override val description = "Two evenings without rice end the regular's visits, including after rice returns."
    override val scenario = "f22/scenario_two_unserved_visits.json"
    override val maxTicks = 96

    override suspend fun run() {
        assertCustomerTrace(
            listOf(ARRIVAL_FIRST_EVENING, "1/1 rating 1 1 NEGATIVE", ARRIVAL_SECOND_EVENING, "2/1 rating 1 1 NEGATIVE"),
        )
    }
}

/** F23: unscheduled evenings and ticks must not create decisions, arrivals or ratings. */
class F23SparseEveningsDecideOnlyAtVisitingTick : AddedCustomerBoundaryTest() {
    override val name = "F23SparseEveningsDecideOnlyAtVisitingTick"
    override val description = "A casual visits at tick three on evenings two and four only, and rates each visit."
    override val scenario = "f23/scenario_sparse_visiting_evenings.json"
    override val maxTicks = 120

    override suspend fun run() {
        assertCustomerTrace(
            listOf(
                "2/3 decision 1 1",
                "2/3 arrival 1 1",
                "2/5 rating 1 1 POSITIVE",
                "4/3 decision 1 1",
                "4/3 arrival 1 1",
                "4/5 rating 1 1 POSITIVE",
            ),
        )
    }
}

/** F25: the restaurant must have an edible dish for each subgroup, not merely for one of them. */
class F25EveryMemberNeedsAnEdibleDish : AddedCustomerBoundaryTest() {
    override val name = "F25EveryMemberNeedsAnEdibleDish"
    override val description = "The rice-only favourite is rejected for a mixed group; different edible dishes suffice."
    override val food = "f25/food_split_preferences.json"
    override val restaurants = "f25/restaurants_split_preferences.json"
    override val scenario = "f25/scenario_split_preferences.json"
    override val maxTicks = 6

    override suspend fun run() {
        assertCustomerTrace(listOf("1/1 decision 1 2"), decisionsOnly = true)
    }
}

/** F25: available seat count allows browsing, but the selected table fails the occupancy rule. */
class F25FailedSeatingDoesNotTriggerAnotherRestaurantDecision : AddedCustomerBoundaryTest() {
    override val name = "F25FailedSeatingDoesNotTriggerAnotherRestaurantDecision"
    override val description = "A group of three rejects an eight-seat table and never switches to restaurant two."
    override val restaurants = "f25/restaurants_seating_failure.json"
    override val scenario = "f25/scenario_no_reconsideration.json"
    override val maxTicks = 24

    override suspend fun run() {
        assertCustomerTrace(listOf("1/3 decision 1 1", "1/3 arrival 1 1", "1/3 rating 1 1 NEGATIVE"))
    }
}

/** F25: future opening time and evening-specific reservation capacity determine event decisions. */
class F25EventUsesFutureOpeningAndReservationCapacity : AddedCustomerBoundaryTest() {
    override val name = "F25EventUsesFutureOpeningAndReservationCapacity"
    override val description = "One event fills evening four; another is rejected, while evening five remains bookable."
    override val restaurants = "f25/restaurants_future_event.json"
    override val scenario = "f25/scenario_future_event_capacity.json"
    override val maxTicks = 25

    override suspend fun run() {
        assertCustomerTrace(listOf("1/1 decision 1 2", "1/1 none 2", "2/1 decision 3 2"), decisionsOnly = true)
    }
}
