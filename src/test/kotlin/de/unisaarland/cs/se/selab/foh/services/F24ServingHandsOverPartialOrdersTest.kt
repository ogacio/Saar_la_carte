package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
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
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * F24 and forum #6: a delivery order bigger than what the waitstaff can carry in one tick is handed
 * to its driver in parts. The driver keeps the order and only drives once every meal is on board,
 * so the desk must not mark it departed in between. A waiter's SERVING budget is shared between the
 * tables it serves and the drivers it loads.
 */
class F24ServingHandsOverPartialOrdersTest {

    private val recipe = Recipe(1, "basic1", 10, setOf(CookType.EXEC), mutableListOf(), RestaurantType.EUROPEAN)

    private fun cookedMeals(group: CustomerGroup) =
        group.members().map { Meal(null, it, recipe, status = MealStatus.COOKED) }.toMutableList()

    private fun deliveryOrder(group: CustomerGroup): Order {
        val meals = cookedMeals(group)
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun deskWith(vararg orders: Order): Pair<DeliveryDesk, DeliveryDriver> {
        val driver = DeliveryDriver(RESTAURANT_ID)
        val desk = DeliveryDesk(mutableListOf(driver), RESTAURANT_ID)
        orders.forEach {
            desk.enqueue(it)
            desk.readyOrder(it)
        }
        return desk to driver
    }

    private fun service(desk: DeliveryDesk, vararg waiters: Waiter) =
        ServingService(WaiterAssignmentService(waiters.toMutableList()), desk, RestaurantType.EUROPEAN)

    @BeforeTest
    fun freshEveningAndTick() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun anOrderBiggerThanOneTicksCapacityStaysAtTheDeskUntilItIsComplete() {
        val group = casual(1, 12, deliveryDistance = 5)
        val order = deliveryOrder(group)
        val (desk, driver) = deskWith(order)
        val waiter = Waiter()

        service(desk, waiter).serve(emptyList(), subUnits())

        assertFalse(driver.hasDeparted(), "10 of the 12 meals are on board, so it waits")
        assertEquals(2, driver.pendingMeals().size)
        assertTrue(desk.getReady().contains(order), "the desk keeps offering the rest to the waitstaff")
    }

    @Test
    fun theRestOfTheOrderGoesOutInTheNextTickAndTheDeskLetsItGo() {
        val group = casual(1, 12, deliveryDistance = 5)
        val order = deliveryOrder(group)
        val (desk, driver) = deskWith(order)
        val waiter = Waiter()
        val serving = service(desk, waiter)
        serving.serve(emptyList(), subUnits())

        GlobalClock.advanceTick()
        waiter.beginTick()
        serving.serve(emptyList(), subUnits())

        assertTrue(driver.hasDeparted(), "the last meal starts the journey")
        assertTrue(driver.pendingMeals().isEmpty())
        assertFalse(desk.getReady().contains(order), "and the desk stops offering it")
    }

    @Test
    fun oneWaiterSplitsItsBudgetBetweenATableAndADriver() {
        val tableGroup = casual(2, 4)
        val visit = Visit(tableGroup)
        val tick = GlobalClock.getTickInEvening()
        // A table's permanent waiter is logged by id, which the assignment service normally grants.
        val waiter = Waiter().also { it.id = 1 }
        visit.seated(Table(1, 4, tableGroup.tableType()), listOf(waiter), tick)
        val tableMeals = cookedMeals(tableGroup)
        visit.ordered(Order(tableGroup, RESTAURANT_ID, tableGroup.id(), tick, false, tableMeals), tick)

        val deliveryGroup = casual(3, 4, deliveryDistance = 5)
        val (desk, driver) = deskWith(deliveryOrder(deliveryGroup))
        val log = captureLog()

        service(desk, waiter).serve(listOf(visit), subUnits())

        assertTrue(driver.hasDeparted(), "4 + 4 fits inside the ten action limit")
        val status = logLines(log).single { it.contains("FOH Serving Status") }
        assertTrue(status.contains("1 waitstaff served 8 meals"), status)
    }

    @Test
    fun theLowestOrderIdIsLoadedFirst() {
        val first = deliveryOrder(casual(7, 4, deliveryDistance = 5))
        val second = deliveryOrder(casual(4, 4, deliveryDistance = 5))
        val driver = DeliveryDriver(RESTAURANT_ID)
        val desk = DeliveryDesk(mutableListOf(driver), RESTAURANT_ID)
        // enqueued out of id order on purpose
        listOf(second, first).forEach {
            desk.enqueue(it)
            desk.readyOrder(it)
        }
        val log = captureLog()

        service(desk, Waiter()).serve(emptyList(), subUnits())

        val handovers = logLines(log).filter { it.contains("FOH Delivery") }
        assertEquals(1, handovers.size, "only one driver, so only one order goes out")
        assertTrue(
            handovers[0].contains("for order ${first.getId()}"),
            "the order with the lowest id takes precedence: ${handovers[0]}",
        )
    }

    @Test
    fun anExhaustedWaitstaffLoadsNobody() {
        val group = casual(5, 2, deliveryDistance = 5)
        val (desk, driver) = deskWith(deliveryOrder(group))
        val waiter = Waiter().also { it.consume(ActionType.SERVING, 10) }

        service(desk, waiter).serve(emptyList(), subUnits())

        assertFalse(driver.hasDeparted(), "there is no capacity left this tick")
        assertTrue(desk.getReady().isNotEmpty())
    }
}
