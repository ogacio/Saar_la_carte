package de.unisaarland.cs.se.selab.foh

/*
 * ============================================================================
 *  Six rules of the specification that no test covered yet.
 * ============================================================================
 *
 *  Written from the specification, not from our code. Each test states what the
 *  simulation owes the customer. The assertions only use what a customer could
 *  see: log lines, who is still in the restaurant, what a waiter carries. No
 *  test looks at a visit's internal state.
 * ============================================================================
 */

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FohSpecTest {

    private fun basicRecipe() =
        Recipe(1, "basic1", 10, setOf(CookType.EXEC), mutableListOf(), RestaurantType.EUROPEAN)

    /** A group that sits at table [tableId] and has ordered one meal per member in the current tick. */
    private fun seatedAndOrdered(
        group: CustomerGroup,
        waiters: List<Waiter>,
        meals: MutableList<Meal>,
        tableId: Int = 1,
    ): Visit {
        val visit = Visit(group)
        val table = Table(tableId, group.groupSize(), TableType.COMMON)
        val tick = GlobalClock.currentTick
        visit.seated(table, waiters, tick)
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        visit.ordered(order, tick)
        return visit
    }

    private fun servingService(desk: DeliveryDesk, vararg waiters: Waiter) =
        ServingService(WaiterAssignmentService(waiters.toMutableList()), desk, RestaurantType.EUROPEAN)

    private fun emptyDesk() = DeliveryDesk(mutableListOf(), RESTAURANT_ID)

    /** Registers one delivery driver for this restaurant for the duration of [block]. */
    private fun withRegisteredDriver(desk: DeliveryDesk, block: () -> Unit) {
        val foh = mock<FrontOfTheHouse>()
        whenever(foh.getDeliveryDesk()).thenReturn(desk)
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getFoh()).thenReturn(foh)
        val sim = mock<Simulator>()
        whenever(sim.restaurantsById(RESTAURANT_ID)).thenReturn(restaurant)
        DeliveryService.setSimulator(sim)
        DeliveryService.addDriver(RESTAURANT_ID)
        try {
            block()
        } finally {
            DeliveryService.rmDriver(RESTAURANT_ID)
        }
    }

    @BeforeTest
    fun freshEveningAndTick() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /**
     * "Customers ... wait for up to 5 ticks for their food ... If at least one person on the table
     *  has received their meal, they wait for 2 more ticks", and "After the customers of a table
     *  have eaten, the waitstaff collects payment, ESCORTS the customers outside".
     *
     * One of three guests is served and eats up. The other two give up seven ticks after ordering.
     * From that moment the table has nobody left who is waiting or eating, so the guest who ate has
     * to be walked outside - the table cannot stay taken for the rest of the evening.
     */
    @Test
    fun theGuestWhoAteIsEscortedOnceTheOthersHaveGivenUp() {
        val recipe = basicRecipe()
        val group = regular(1, 3)
        val waiter = Waiter().also { it.id = 1 }
        waiter.adjustLoad(3)
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val visit = seatedAndOrdered(group, listOf(waiter), meals)
        // one meal is cooked and carried to the table right away
        meals[0].status = MealStatus.COOKED
        visit.serve(listOf(meals[0]), GlobalClock.currentTick)

        val dining = DiningService()
        val escorting = EscortingService(WaiterAssignmentService(mutableListOf(waiter)))
        repeat(PATIENCE + EXTRA_PATIENCE) {
            GlobalClock.advanceTick()
            waiter.beginTick()
            dining.eat(listOf(visit), subUnits())
        }
        val log = captureLog()
        escorting.escort(listOf(visit), subUnits())

        assertTrue(
            logLines(log).any { it.contains("FOH Escorting") && it.contains("1 customers of group 1") },
            "the guest who had eaten was never escorted out: ${logLines(log)}",
        )
        assertTrue(visit.customersInside().isEmpty(), "the table is still taken")
    }

    /**
     * "If the assigned waiter does not have enough SERVING capacity to serve the full table even
     *  though the meals have been cooked, the waiter will wait for the next tick and then start
     *  SERVING the meals to the table one by one."
     *
     * The waiter has one SERVING action left in each of the two ticks. In the first tick the table
     * of three has to wait. In the second one the waiter no longer waits for the full table: one
     * meal goes out.
     */
    @Test
    fun aTableThatWaitedOneTickForCapacityIsThenServedOneByOne() {
        val recipe = basicRecipe()
        val group = regular(2, 3)
        val waiter = Waiter().also { it.id = 1 }
        val meals = group.members()
            .map { Meal(null, it, recipe, status = MealStatus.COOKED) }
            .toMutableList()
        val visit = seatedAndOrdered(group, listOf(waiter), meals)
        val service = servingService(emptyDesk(), waiter)

        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val firstTick = captureLog()
        service.serve(listOf(visit), subUnits())

        assertTrue(
            logLines(firstTick).none { it.contains("FOH Serving (") },
            "the table of three was served although only one meal could be carried",
        )

        GlobalClock.advanceTick()
        waiter.beginTick()
        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val secondTick = captureLog()
        service.serve(listOf(visit), subUnits())

        assertTrue(
            logLines(secondTick).any { it.contains("FOH Serving (") && it.contains("serves basic1:1") },
            "after waiting one tick the meals go out one by one: ${logLines(secondTick)}",
        )
    }

    /**
     * "The waiters with the lowest id starts, and the order with the lowest id takes precedence."
     *
     * Two complete delivery orders wait, one driver is free. The older order - the lower id - is the
     * one that leaves the restaurant.
     */
    @Test
    fun theDeliveryOrderWithTheLowestIdIsHandedOverFirst() {
        val recipe = basicRecipe()
        val desk = emptyDesk()
        val early = deliveryOrder(recipe, 4)
        val late = deliveryOrder(recipe, 5)
        // the desk is offered the later order first, to make sure the id decides and not the order
        // in which they arrived
        listOf(late, early).forEach {
            desk.enqueue(it)
            desk.readyOrder(it)
        }
        val waiter = Waiter().also { it.id = 1 }
        val service = servingService(desk, waiter)

        val log = captureLog()
        withRegisteredDriver(desk) { service.serve(emptyList(), subUnits()) }

        val deliveries = logLines(log).filter { it.contains("FOH Delivery") }
        assertEquals(1, deliveries.size, "one driver can take one order: $deliveries")
        assertTrue(
            deliveries.single().contains("for order ${early.getId()}."),
            "the order with the lowest id has to go first: ${deliveries.single()}",
        )
    }

    private fun deliveryOrder(recipe: Recipe, groupId: Int): Order {
        val group = regular(groupId, 1)
        val meals = group.members()
            .map { Meal(null, it, recipe, status = MealStatus.COOKED) }
            .toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), GlobalClock.currentTick, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    /**
     * "In case there are meals the waitstaff could have served but did not due to waiting for other
     *  meals of the table ... the simulation logs these. For tables that serve event customer
     *  groups, they log the last waiter that did serve or the first that would have served."
     *
     * An EVENT table is held back because one of its two meals is still in the pan, and no waiter of
     * this restaurant has done anything yet this evening. The line still has to name a waiter - the
     * one who would have served - and ids start at 1 per restaurant and evening.
     */
    @Test
    fun anEventTableThatIsHeldBackNamesTheWaiterThatWouldHaveServed() {
        val recipe = basicRecipe()
        val group = event(3, 2)
        val meals = mutableListOf(
            Meal(null, group.members()[0], recipe, status = MealStatus.COOKED),
            Meal(null, group.members()[1], recipe, status = MealStatus.COOKING),
        )
        // EVENT groups have no permanent waiter; the manager picks one per action
        val visit = seatedAndOrdered(group, emptyList(), meals, tableId = 7)
        val service = servingService(emptyDesk(), Waiter(), Waiter())

        val log = captureLog()
        service.serve(listOf(visit), subUnits())

        val expected = "[DEBUG] FOH No Serving (R $RESTAURANT_ID): " +
            "Waitstaff 1 did not serve 1 meals to table 7."
        assertTrue(
            logLines(log).contains(expected),
            "the event table reported no waiter at all: ${logLines(log)}",
        )
    }

    /**
     * "If a restaurant has reached the end of the opening time and customers are still inside, you
     *  can assume that they are all escorted outside immediately. Those groups that have finished
     *  eating see this as a non-negative experience, for all other groups this counts as a negative
     *  experience."
     *
     * A group that is still waiting for a table when the doors close never ate, so it leaves with a
     * negative experience - and it must not stay in the restaurant over the night.
     */
    @Test
    fun aGroupStillWaitingForASeatLeavesNegativeWhenTheOpeningTimeEnds() {
        val visit = Visit(regular(4, 2))

        visit.sendOut()

        assertTrue(visit.customersInside().isEmpty(), "the group is still in the restaurant")
        assertTrue(visit.isFinished(), "the visit did not end")
        assertEquals(Experience.NEGATIVE, visit.experience(), "a group that never ate is not happy")
    }

    /**
     * Same rule, one step later: the group sat down and ordered, but the food never came. The
     * customers leave, and the waiter is no longer waiting on anybody - "the current load is the
     * amount of customers a waiter is waiting on ... before they are ESCORTED out".
     */
    @Test
    fun aSeatedGroupThatNeverGotFoodLeavesAndFreesItsWaiter() {
        val recipe = basicRecipe()
        val group = regular(5, 2)
        val waiter = Waiter().also { it.id = 1 }
        waiter.adjustLoad(2)
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val visit = seatedAndOrdered(group, listOf(waiter), meals)

        visit.sendOut()

        assertTrue(visit.customersInside().isEmpty())
        assertEquals(Experience.NEGATIVE, visit.experience())
        assertEquals(0, waiter.currentLoad, "the waiter is still waiting on guests who left")
    }

    private companion object {
        /** "Customers ... wait for up to 5 ticks for their food to be SERVED." */
        const val PATIENCE = 5

        /** "If at least one person on the table has received their meal, they wait for 2 more ticks." */
        const val EXTRA_PATIENCE = 2
    }
}
