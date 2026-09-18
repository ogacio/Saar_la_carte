package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F19: serving beyond a single table - delivery orders that finish cooking, the EVENT manager ranking the
 * waiters by the cooked meals waiting for their own tables, and a table that has no waiter to serve it.
 */
class ServingServiceDeliveryAndEventTest {

    private val recipe = Recipe(1, "basic1", 10, setOf(CookType.EXEC), mutableListOf(), RestaurantType.EUROPEAN)

    private fun meals(group: CustomerGroup, status: MealStatus = MealStatus.COOKED) =
        group.members().map { Meal(null, it, recipe, status = status) }.toMutableList()

    /** A group seated at table [tableId] by [waiters] that has ordered [meals] in this tick. */
    private fun orderedVisit(
        group: CustomerGroup,
        tableId: Int,
        waiters: List<Waiter>,
        meals: MutableList<Meal>,
    ): Visit {
        val visit = Visit(group)
        val tick = GlobalClock.getTickInEvening()
        visit.seated(Table(tableId, group.members().size, group.tableType()), waiters, tick)
        visit.ordered(Order(group, RESTAURANT_ID, group.id(), tick, false, meals), tick)
        return visit
    }

    private fun deliveryOrder(group: CustomerGroup, meals: MutableList<Meal>) =
        Order(group, RESTAURANT_ID, group.id(), 1, true, meals)

    private fun service(desk: DeliveryDesk, vararg waiters: Waiter) =
        ServingService(WaiterAssignmentService(waiters.toMutableList()), desk, RestaurantType.EUROPEAN)

    @BeforeTest
    fun freshEveningAndTick() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun onlyDeliveryOrdersWhoseMealsAreAllCookedBecomeReady() {
        val cookedGroup = casual(8, 2, deliveryDistance = 3)
        val cooked = deliveryOrder(cookedGroup, meals(cookedGroup))
        val cookingGroup = casual(9, 2, deliveryDistance = 3)
        val cookingMeals = meals(cookingGroup)
        cookingMeals[1].status = MealStatus.COOKING
        val cooking = deliveryOrder(cookingGroup, cookingMeals)
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        desk.enqueue(cooked)
        desk.enqueue(cooking)

        // Without waitstaff nobody can hand the ready order to a driver, so it stays at the desk.
        service(desk).serve(emptyList(), subUnits())

        assertEquals(listOf(cooked), desk.getReady())
        assertEquals(listOf(cooking), desk.getNewOrders())
    }

    @Test
    fun eventTableIsServedFirstByTheWaiterWithTheMostCookedMealsForTheirOwnTables() {
        val first = Waiter().also { it.id = 1 }
        val second = Waiter().also { it.id = 2 }
        val eventGroup = event(3, 2)
        val eventVisit = orderedVisit(eventGroup, EVENT_TABLE, emptyList(), meals(eventGroup))
        // Waiter 2 has two cooked meals waiting for his tables, waiter 1 only one.
        val secondsTables = listOf(casual(4, 1), casual(5, 1)).mapIndexed { index, group ->
            orderedVisit(group, index + 1, listOf(second), meals(group))
        }
        val firstsGroup = casual(6, 1)
        val firstsTable = orderedVisit(firstsGroup, 3, listOf(first), meals(firstsGroup))
        val log = captureLog()

        service(DeliveryDesk(mutableListOf(), RESTAURANT_ID), first, second)
            .serve(listOf(eventVisit) + secondsTables + firstsTable, subUnits())

        val eventServing = logLines(log).filter {
            it.contains("FOH Serving (R 1)") && it.contains("table $EVENT_TABLE ")
        }
        assertEquals(1, eventServing.size)
        assertTrue(eventServing.single().contains("Waitstaff 2 serves"))
        assertTrue(logLines(log).any { it.contains("FOH Serving Status") && it.contains("2 waitstaff served 5 meals") })
    }

    @Test
    fun tableWithoutAWaiterIsNeverServedAndNoWaiterIsNamed() {
        val group = casual(3, 2)
        val tableMeals = meals(group)
        val visit = orderedVisit(group, 1, emptyList(), tableMeals)
        val service = service(DeliveryDesk(mutableListOf(), RESTAURANT_ID))
        val log = captureLog()

        service.serve(listOf(visit), subUnits())
        GlobalClock.advanceTick()
        GlobalClock.advanceTick()
        service.serve(listOf(visit), subUnits())

        assertTrue(tableMeals.all { it.status == MealStatus.COOKED })
        assertTrue(logLines(log).none { it.contains("FOH Serving (R 1)") || it.contains("FOH No Serving") })
    }

    private companion object {
        const val EVENT_TABLE = 9
    }
}
