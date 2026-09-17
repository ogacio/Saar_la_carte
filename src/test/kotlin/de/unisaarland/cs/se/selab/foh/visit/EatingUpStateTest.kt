package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Eating, from the specification: "Once a customer has received their meal, they take 2 full ticks to
 * eat it." Only when everyone at the table has finished does the group wait to be escorted out.
 */
class EatingUpStateTest {

    /** A group that received all its meals at [servedTick]. */
    private fun eatingVisit(group: CustomerGroup, servedTick: Int): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(Waiter()), servedTick)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), servedTick, false, meals)
        meals.forEach {
            it.orderId = order.getId()
            it.status = MealStatus.COOKED
        }
        visit.ordered(order, servedTick)
        visit.serve(visit.cookedMeals(), servedTick)
        return visit
    }

    @Test
    fun beforeTwoFullTicksNobodyIsDone() {
        val visit = eatingVisit(regular(1, 2), servedTick = 1)

        visit.advance(1 + EATING_TICKS - 1)

        assertIs<EatingUpState>(visit.state)
        assertEquals(0, visit.finishedEatingThisTick)
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.SERVED })
    }

    @Test
    fun afterTwoFullTicksTheWholeTableIsDoneAndWaitsToBeEscorted() {
        val visit = eatingVisit(regular(1, 2), servedTick = 1)

        visit.advance(1 + EATING_TICKS)

        assertIs<ReadyToLeaveState>(visit.state)
        assertEquals(2, visit.finishedEatingThisTick)
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.DONE_EATING })
    }

    @Test
    fun theFinishedCounterCountsOnlyTheCurrentTick() {
        val visit = eatingVisit(regular(1, 2), servedTick = 1)
        visit.advance(1 + EATING_TICKS)

        visit.advance(1 + EATING_TICKS + 1)

        assertEquals(0, visit.finishedEatingThisTick)
        assertIs<ReadyToLeaveState>(visit.state)
    }

    private companion object {
        const val EATING_TICKS = 2
    }
}
