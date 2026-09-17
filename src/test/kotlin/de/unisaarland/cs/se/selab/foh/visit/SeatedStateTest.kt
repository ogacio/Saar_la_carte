package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The phase between seating and ordering: "Immediately after SEATING the customers on the table, the
 * customers order as a group". From the order tick on, every waiting deadline of the specification is
 * counted. If nobody in the group finds a dish, the group leaves the restaurant.
 */
class SeatedStateTest {

    private fun seatedVisit(group: CustomerGroup, tick: Int = 1): Visit {
        val visit = Visit(group)
        visit.seated(Table(1, group.groupSize(), TableType.COMMON), listOf(Waiter()), tick)
        return visit
    }

    private fun orderFor(group: CustomerGroup, tick: Int): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    @Test
    fun orderingStoresTheOrderAndStartsTheWaitingClock() {
        val group = regular(1, 2)
        val visit = seatedVisit(group)
        val order = orderFor(group, tick = 3)

        visit.ordered(order, 3)

        assertIs<AwaitingMealState>(visit.state)
        assertEquals(order, visit.order)
        assertEquals(3, visit.orderedTick)
    }

    @Test
    fun withoutAnyDishTheWholeGroupLeaves() {
        val visit = seatedVisit(regular(1, 2))

        visit.orderingFailed(3)

        assertIs<GoneState>(visit.state)
        assertTrue(visit.isFinished())
        assertTrue(visit.customersInside().isEmpty())
    }

    @Test
    fun aSeatedGroupIgnoresServingAndEatingEvents() {
        val group = regular(1, 2)
        val visit = seatedVisit(group)

        visit.serve(orderFor(group, 1).getMeals(), 2)
        visit.advance(2)

        assertIs<SeatedState>(visit.state)
        assertEquals(0, visit.servedCustomers())
    }
}
