package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The visit itself: the hold-back rule for cooked meals ("after the first meal has been cooked, the
 * table is not SERVED for this and the following tick unless all other meals have been cooked"), the
 * experience of the group ("customers expect food within 4 ticks"; positive before the window, neutral
 * at its end, negative later or when food is missing) and the end of the opening time, where everyone
 * still inside leaves at once and a group that had finished eating keeps a non-negative experience.
 */
class VisitTest {

    private fun orderedVisit(group: CustomerGroup, tick: Int, waiters: List<Waiter> = listOf(Waiter())): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), waiters, tick)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        visit.ordered(order, tick)
        return visit
    }

    private fun cook(visit: Visit, count: Int) {
        visit.order?.getMeals().orEmpty().filter { it.status == MealStatus.QUEUED }.take(count)
            .forEach { it.status = MealStatus.COOKED }
    }

    @Test
    fun anIncompleteTableIsHeldBackForTwoTicks() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 1)
        visit.firstMealTick = 1

        assertTrue(visit.servableMeals(1).isEmpty())
        assertTrue(visit.servableMeals(2).isEmpty())
        assertEquals(1, visit.servableMeals(3).size)
    }

    @Test
    fun aCompleteTableMayBeServedAtOnce() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 2)
        visit.firstMealTick = 1

        assertEquals(2, visit.servableMeals(1).size)
    }

    @Test
    fun foodWithinTheExpectationWindowIsAPositiveExperience() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 2)

        visit.serve(visit.cookedMeals(), 1 + EXPECTED - 1)

        assertEquals(Experience.POSITIVE, visit.experience())
    }

    @Test
    fun foodExactlyAtTheEndOfTheWindowIsNeutral() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 2)

        visit.serve(visit.cookedMeals(), 1 + EXPECTED)

        assertEquals(Experience.NEUTRAL, visit.experience())
    }

    @Test
    fun foodAfterTheWindowOrMissingFoodIsNegative() {
        val late = orderedVisit(regular(1, 2), tick = 1)
        cook(late, 2)
        late.serve(late.cookedMeals(), 1 + EXPECTED + 1)

        val partly = orderedVisit(regular(2, 2), tick = 1)
        cook(partly, 1)
        partly.serve(partly.cookedMeals(), 2)

        assertEquals(Experience.NEGATIVE, late.experience())
        assertEquals(Experience.NEGATIVE, partly.experience())
    }

    @Test
    fun aGroupThatNeverOrderedHasANegativeExperience() {
        val visit = Visit(regular(1, 2))

        assertEquals(Experience.NEGATIVE, visit.experience())
    }

    @Test
    fun atTheEndOfOpeningTimeEveryoneLeavesAtOnce() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 1)
        visit.serve(visit.cookedMeals(), 2)

        visit.sendOut()

        assertIs<GoneState>(visit.state)
        assertTrue(visit.customersInside().isEmpty())
        assertEquals(Experience.NEGATIVE, visit.experience())
    }

    @Test
    fun aGroupSentOutAfterEatingKeepsANonNegativeExperience() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        cook(visit, 2)
        visit.serve(visit.cookedMeals(), 1 + EXPECTED + 1)
        visit.advance(1 + EXPECTED + 1 + EATING_TICKS)

        visit.sendOut()

        assertEquals(Experience.NEUTRAL, visit.experience())
    }

    @Test
    fun customersLeavingWithoutFoodFreeTheWaiterOfTheirLoad() {
        val waiter = Waiter()
        waiter.adjustLoad(2)
        val visit = orderedVisit(regular(1, 2), tick = 1, waiters = listOf(waiter))

        visit.leaveUnserved(visit.customersInside())

        assertEquals(0, waiter.currentLoad)
        // the kitchen is not told, so the meals stay queued; they are simply never served
        assertTrue(visit.order?.getMeals().orEmpty().all { it.status == MealStatus.QUEUED })
        assertTrue(visit.cookedMeals().isEmpty(), "nothing is waiting for a waiter any more")
    }

    @Test
    fun eventCustomersNeverCountTowardsAWaitersLoad() {
        val waiter = Waiter()
        val visit = orderedVisit(event(3, 4), tick = 1, waiters = listOf(waiter))

        visit.leaveUnserved(visit.customersInside())

        assertEquals(0, waiter.currentLoad)
    }

    @Test
    fun sendingOutEatingCustomersFreesTheWaiterOfTheirLoad() {
        val waiter = Waiter()
        waiter.adjustLoad(2)
        val visit = orderedVisit(regular(1, 2), tick = 1, waiters = listOf(waiter))
        cook(visit, 2)
        visit.serve(visit.cookedMeals(), 2)

        visit.sendOut()

        // The load counts customers "before they are ESCORTED out", so it drops for the whole table.
        assertEquals(0, waiter.currentLoad)
        assertTrue(visit.customersInside().isEmpty())
    }

    @Test
    fun sendingOutAMixedTableCountsEveryCustomerExactlyOnce() {
        val waiter = Waiter()
        waiter.adjustLoad(3)
        val visit = orderedVisit(regular(1, 3), tick = 1, waiters = listOf(waiter))
        cook(visit, 1)
        // One customer eats, one has finished eating, one is still waiting for food.
        visit.serve(visit.cookedMeals(), 2)
        cook(visit, 1)
        visit.serve(visit.cookedMeals(), 2)
        visit.advance(2 + EATING_TICKS)

        visit.sendOut()

        assertEquals(0, waiter.currentLoad)
        assertTrue(visit.customersInside().isEmpty())
    }

    @Test
    fun sendingOutDoesNotTouchTheLoadOfAnEventGroup() {
        val waiter = Waiter()
        waiter.adjustLoad(4)
        val visit = orderedVisit(event(3, 4), tick = 1, waiters = listOf(waiter))
        cook(visit, 4)
        visit.serve(visit.cookedMeals(), 2)

        visit.sendOut()

        // EVENT customers never counted towards the load, so the other customers of this waiter stay.
        assertEquals(4, waiter.currentLoad)
    }

    @Test
    fun sendingOutAnAlreadyFinishedVisitChangesNothing() {
        val waiter = Waiter()
        waiter.adjustLoad(2)
        val visit = orderedVisit(regular(1, 2), tick = 1, waiters = listOf(waiter))
        cook(visit, 2)
        visit.serve(visit.cookedMeals(), 2)
        visit.sendOut()

        visit.sendOut()

        assertEquals(0, waiter.currentLoad)
        assertIs<GoneState>(visit.state)
    }

    private companion object {
        /** "After ordering in a restaurant, customers expect food within 4 ticks." */
        const val EXPECTED = 4

        const val EATING_TICKS = 2
    }
}
