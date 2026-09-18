package de.unisaarland.cs.se.selab.foh

/*
 * The waiter himself: ids, the action limit and the two loads.
 * Drop into src/test/kotlin/de/unisaarland/cs/se/selab/foh/.
 *
 * There is a WaiterAssignmentServiceTest, but the class it assigns has no test of its own, and
 * every rule of the front of house is counted in these three numbers.
 *
 * Written from the specification:
 *   "they do not have ids assigned at the start of the simulation ... you will assign them ids
 *    starting from id = 1 per restaurant at the moment they perform their first action each
 *    evening. Each waiter has a limit of 10 actions per type and tick, so for example they cannot
 *    SEAT more than 10 people to a table in that tick."
 *   "The current load is the amount of customers a waiter is waiting on, so any customer this
 *    waiter has SEATED before they are ESCORTED out of the restaurant. EVENTS do not count toward
 *    that load. The tick load is defined per action type and describes how many actions of that
 *    type the waiter has performed that tick."
 */

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WaiterTest {

    /** "they do not have ids assigned at the start of the simulation" */
    @Test
    fun aFreshWaiterHasNoIdAndWaitsOnNobody() {
        val waiter = Waiter()

        assertNull(waiter.id)
        assertEquals(0, waiter.currentLoad)
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SEATING))
    }

    /** "a limit of 10 actions per type and tick" - per TYPE, so the four counters are independent. */
    @Test
    fun theLimitIsCountedPerActionType() {
        val waiter = Waiter()

        waiter.consume(ActionType.SEATING, Waiter.ACTION_LIMIT)

        assertEquals(0, waiter.remaining(ActionType.SEATING), "this waiter cannot seat anybody else")
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.ORDERING), "but may still take orders")
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.SERVING))
        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.ESCORTING))
    }

    /**
     * "they cannot SEAT more than 10 people to a table in that tick" - whatever a caller asks for,
     * a waiter never performs more than ten actions of one type in one tick, and the room left is
     * never a negative number.
     */
    @Test
    fun aWaiterNeverPerformsMoreThanTenActionsOfATypeInOneTick() {
        val waiter = Waiter()

        waiter.consume(ActionType.SERVING, 6)
        waiter.consume(ActionType.SERVING, 6)

        assertEquals(0, waiter.remaining(ActionType.SERVING), "twelve actions in one tick are not possible")
    }

    /** "The tick load ... describes how many actions of that type the waiter has performed that tick." */
    @Test
    fun theTickLoadStartsAtZeroAgainInTheNextTick() {
        val waiter = Waiter()
        waiter.consume(ActionType.ESCORTING, Waiter.ACTION_LIMIT)

        waiter.beginTick()

        assertEquals(Waiter.ACTION_LIMIT, waiter.remaining(ActionType.ESCORTING))
    }

    /**
     * "The current load is the amount of customers a waiter is waiting on, so any customer this
     *  waiter has SEATED before they are ESCORTED out of the restaurant."
     */
    @Test
    fun theCurrentLoadFollowsTheCustomersInTheRestaurant() {
        val waiter = Waiter()

        waiter.adjustLoad(5)
        waiter.adjustLoad(-2)

        assertEquals(3, waiter.currentLoad)
    }

    /** A waiter cannot wait on a negative number of customers, however often guests leave. */
    @Test
    fun theCurrentLoadNeverFallsBelowZero() {
        val waiter = Waiter()
        waiter.adjustLoad(2)

        waiter.adjustLoad(-5)

        assertEquals(0, waiter.currentLoad)
    }

    /** "ids ... per restaurant at the moment they perform their first action each evening" */
    @Test
    fun theEveningResetDropsTheIdAndBothLoads() {
        val waiter = Waiter()
        waiter.id = 3
        waiter.adjustLoad(4)
        waiter.consume(ActionType.SEATING, 7)

        waiter.resetEvening()

        assertNull(waiter.id, "tomorrow this waiter is nameless again")
        assertEquals(0, waiter.currentLoad)
        assertEquals(
            Waiter.ACTION_LIMIT,
            waiter.remaining(ActionType.SEATING),
            "the new evening starts with the full action limit"
        )
    }
}
