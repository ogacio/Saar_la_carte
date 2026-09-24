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
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * F19: the serving rules of a visit before and around its first cooked meal. A table without an
 * order or without a cooked meal has nothing to serve and no hold-back window yet. Inside the
 * window a table that nobody has been served at must be served completely ("the table is not
 * SERVED for this and the following tick unless all other meals have been cooked as well"), but
 * only until somebody got food or the table already waited one tick for SERVING capacity.
 */
class F19VisitBeforeTheFirstMealTest {

    private fun seatedVisit(group: CustomerGroup): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(Waiter()), 1)
        return visit
    }

    private fun orderedVisit(group: CustomerGroup, tick: Int): Visit {
        val visit = seatedVisit(group)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        visit.ordered(order, tick)
        return visit
    }

    @Test
    fun aVisitThatHasNotOrderedHasNothingCookedAndNothingToServe() {
        val visit = seatedVisit(regular(1, 2))

        assertTrue(visit.cookedMeals().isEmpty())
        assertTrue(visit.servableMeals(1).isEmpty())
        assertFalse(visit.anyoneServed())
    }

    @Test
    fun beforeTheFirstMealIsCookedThereIsNoHoldBackWindow() {
        val visit = orderedVisit(regular(1, 2), tick = 1)

        assertFalse(visit.inHoldBackWindow(1), "the window starts with the first cooked meal")
        assertFalse(visit.needsCompleteServing(1))
        assertTrue(visit.servableMeals(1).isEmpty())
    }

    @Test
    fun insideTheWindowAnUnservedTableMustBeServedCompletely() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        visit.firstMealTick = 2

        assertTrue(visit.needsCompleteServing(2), "the tick of the first cooked meal")
        assertTrue(visit.needsCompleteServing(3), "and the tick after it")
        assertFalse(visit.needsCompleteServing(4), "after the window the table is served one by one")
    }

    @Test
    fun onceSomebodyHasFoodTheRestOfTheTableIsNoLongerHeldTogether() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        val first = checkNotNull(visit.order).getMeals().first()
        first.status = MealStatus.COOKED
        visit.firstMealTick = 2

        visit.serve(listOf(first), 2)

        assertTrue(visit.anyoneServed())
        assertEquals(1, visit.servedCustomers())
        assertFalse(visit.needsCompleteServing(2))
    }

    @Test
    fun aTableThatAlreadyWaitedForCapacityIsNotHeldBackAgain() {
        val visit = orderedVisit(regular(1, 2), tick = 1)
        visit.firstMealTick = 2

        visit.waitedForCapacity = true

        assertTrue(visit.inHoldBackWindow(2))
        assertFalse(visit.needsCompleteServing(2), "the table waits for capacity only once")
    }

    @Test
    fun aWaitingTableWithoutAnOrderTickNeverRunsOutOfPatience() {
        val visit = seatedVisit(regular(1, 2))
        visit.state = AwaitingMealState()

        visit.advance(PATIENCE_LONG_GONE)

        assertIs<AwaitingMealState>(visit.state, "no deadline is counted without an order tick")
        assertEquals(0, visit.leftUnservedThisTick)
        assertTrue(visit.group.members().none { it.status() == CustomerStatus.LEFT })
    }

    private companion object {
        const val PATIENCE_LONG_GONE = 50
    }
}
