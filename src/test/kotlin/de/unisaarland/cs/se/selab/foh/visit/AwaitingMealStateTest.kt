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
 * Waiting for food, with the deadlines of the specification: the group waits up to 5 ticks for the
 * first meal, and if at least one customer has been served, the others wait 2 more ticks before they
 * leave. Meals that were served belong to their customer, who then eats for 2 full ticks.
 */
class AwaitingMealStateTest {

    private fun waitingVisit(group: CustomerGroup, tick: Int = 1, cooked: Boolean = true): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(Waiter()), tick)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach {
            it.orderId = order.getId()
            if (cooked) it.status = MealStatus.COOKED
        }
        visit.ordered(order, tick)
        return visit
    }

    @Test
    fun servingEveryMealMovesTheTableToEating() {
        val visit = waitingVisit(regular(1, 2))

        visit.serve(visit.cookedMeals(), 2)

        assertIs<EatingUpState>(visit.state)
        assertEquals(2, visit.servedCustomers())
        assertTrue(visit.group.members().all { it.status() == CustomerStatus.SERVED })
    }

    @Test
    fun partialServingKeepsTheTableWaiting() {
        val visit = waitingVisit(regular(1, 3))

        visit.serve(visit.cookedMeals().take(1), 2)

        assertIs<AwaitingMealState>(visit.state)
        assertEquals(1, visit.servedCustomers())
        assertEquals(2, visit.customersWaitingForFood().size)
    }

    @Test
    fun servedMealsAreNotServedAgain() {
        val visit = waitingVisit(regular(1, 2))
        val meals = visit.cookedMeals()

        visit.serve(meals, 2)

        assertTrue(meals.all { it.status == MealStatus.SERVED })
        assertTrue(visit.cookedMeals().isEmpty())
    }

    @Test
    fun withoutAnyMealTheGroupLeavesAfterFiveTicks() {
        val visit = waitingVisit(regular(1, 2), tick = 1)

        visit.advance(PATIENCE - 1)
        assertIs<AwaitingMealState>(visit.state)

        visit.advance(PATIENCE)

        assertIs<GoneState>(visit.state)
        assertTrue(visit.failedAttempt)
        assertEquals(2, visit.leftUnservedThisTick)
    }

    /**
     * Staff worked example (forum topic 335, post #2, Ciprian): ordered tick 2, "counting
     * ticks 2-6 gives five waiting ticks, so it leaves during tick 6, not tick 7" — the
     * 5-tick window is inclusive of the order tick itself.
     */
    @Test
    fun staffWorkedExampleOrderedTickTwoLeavesDuringTickSixNotSeven() {
        val visit = waitingVisit(regular(1, 2), tick = STAFF_EXAMPLE_ORDERED_TICK)

        visit.advance(STAFF_EXAMPLE_ORDERED_TICK + PATIENCE - 1)

        assertIs<GoneState>(visit.state)
        assertTrue(visit.failedAttempt)
    }

    @Test
    fun theTickBeforeTheStaffWorkedExampleStillWaits() {
        val visit = waitingVisit(regular(1, 2), tick = STAFF_EXAMPLE_ORDERED_TICK)

        visit.advance(STAFF_EXAMPLE_ORDERED_TICK + PATIENCE - 2)

        assertIs<AwaitingMealState>(visit.state)
    }

    @Test
    fun aftrTwoExtraTicksOnlyTheUnservedCustomersLeave() {
        val visit = waitingVisit(regular(1, 3), tick = 1)
        visit.serve(visit.cookedMeals().take(1), 2)

        visit.advance(1 + PATIENCE + EXTRA)

        assertEquals(2, visit.leftUnservedThisTick)
        assertEquals(1, visit.customersInside().size)
        assertTrue(!visit.isFinished())
    }

    /**
     * "for this there is no synchronization to the kitchen, so the kitchen simply continues to try
     * and cook their meals": the meals of the customers who left stay in the queue, they are only
     * never carried to a table.
     */
    @Test
    fun theMealsOfCustomersWhoLeftStayInTheKitchen() {
        val visit = waitingVisit(regular(1, 2), tick = 1)

        visit.advance(1 + PATIENCE)

        // the kitchen was never told, so the meals keep the status it gave them ...
        assertTrue(visit.order?.getMeals().orEmpty().all { it.status == MealStatus.COOKED })
        // ... they are simply no longer waiting for a waiter
        assertTrue(visit.cookedMeals().isEmpty())
    }

    private companion object {
        const val PATIENCE = 5
        const val EXTRA = 2
        const val STAFF_EXAMPLE_ORDERED_TICK = 2
    }
}
