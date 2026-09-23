package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * F24/F29: the delivery desk's bookkeeping over drivers in every state - the free-driver count that
 * treats a still-loading driver as free (forum 304), dropping a given-up order, and the evening reset
 * that sends each driver home according to its state.
 */
class DeliveryDeskStateTest {

    private fun desk(vararg drivers: DeliveryDriver) = DeliveryDesk(drivers.toMutableList(), RESTAURANT_ID)

    private fun orderFor(group: CustomerGroup): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun waiting(id: Int) = DeliveryDriver(RESTAURANT_ID).also { it.setId(id) }

    private fun loading(id: Int, order: Order) = DeliveryDriver(RESTAURANT_ID).also {
        it.setId(id)
        it.assignOrder(order)
    }

    private fun delivering(id: Int, order: Order) = DeliveryDriver(RESTAURANT_ID).also {
        it.setId(id)
        it.receiveOrder(order)
    }

    /** Drives [driver] out to the customer and lets it accept the delivery, so it is now RETURNING. */
    private fun returning(id: Int): DeliveryDriver {
        val driver = delivering(id, orderFor(casual(id, 1, deliveryDistance = 5)))
        GlobalClock.advanceTick()
        driver.prepare()
        driver.drive()
        driver.arrive()
        driver.deliverAccepted()
        return driver
    }

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun freeDriverCountTreatsLoadingDriversAsFreeButNotDeliveringOnes() {
        captureLog()
        val desk = desk(
            waiting(1),
            loading(2, orderFor(casual(2, 2, deliveryDistance = 7))),
            delivering(3, orderFor(casual(3, 1, deliveryDistance = 5))),
        )

        assertEquals(2, desk.amountFreeDrivers())
    }

    @Test
    fun droppingAGivenUpOrderReleasesTheStillLoadingDriverAndClearsTheQueues() {
        val order = orderFor(casual(2, 2, deliveryDistance = 7))
        val driver = loading(2, order)
        val desk = desk(driver)
        desk.enqueue(order)
        desk.readyOrder(order)

        desk.drop(order)

        assertTrue(driver.isWaiting())
        assertTrue(desk.getNewOrders().isEmpty())
        assertTrue(desk.getReady().isEmpty())
    }

    @Test
    fun logPreparationForAnUnknownDriverIsANoOp() {
        val desk = desk(waiting(1))
        val log = captureLog()

        desk.logPreparationFor(99)

        assertTrue(logLines(log).isEmpty())
    }

    @Test
    fun logPreparationForALoadedDriverAnnouncesItsRoute() {
        val order = orderFor(casual(3, 1, deliveryDistance = 7))
        val desk = desk(delivering(4, order))
        val log = captureLog()

        desk.logPreparationFor(4)

        assertEquals(
            listOf(
                "[INFO] Delivery Preparation (R 1): Driver 4 prepares driving order ${order.getId()} to group 3, " +
                    "which will take 2 ticks.",
            ),
            logLines(log),
        )
    }

    @Test
    fun logPreparationForSkipsDriversWithoutAnId() {
        val order = orderFor(casual(3, 1, deliveryDistance = 7))
        val desk = desk(DeliveryDriver(RESTAURANT_ID), delivering(4, order))
        val log = captureLog()

        desk.logPreparationFor(4)

        assertEquals(1, logLines(log).size)
    }

    @Test
    fun droppingAnOrderKeepsTheDriverThatAlreadyDroveOff() {
        captureLog()
        val order = orderFor(casual(3, 1, deliveryDistance = 5))
        val driver = delivering(3, order)
        val desk = desk(driver)

        desk.drop(order)

        // It reports the failed delivery on arrival instead of being released now.
        assertTrue(driver.isDelivering())
    }

    @Test
    fun theDriverChosenForAnOrderKeepsItUntilItIsComplete() {
        captureLog()
        val busy = delivering(7, orderFor(casual(7, 1, deliveryDistance = 5)))
        val free = DeliveryDriver(RESTAURANT_ID)
        val desk = desk(busy, free)
        registerWithDeliveryService(desk)
        val order = orderFor(casual(3, 2, deliveryDistance = 7))

        val chosen = desk.driverFor(order)

        assertSame(free, chosen)
        assertEquals(1, free.getId())
        assertTrue(free.isLoading())
        assertSame(free, desk.driverFor(order))
    }

    /** Lets the delivery service resolve every restaurant id to [desk]. */
    private fun registerWithDeliveryService(desk: DeliveryDesk) {
        val foh = mock<FrontOfTheHouse> { on { getDeliveryDesk() } doReturn desk }
        val restaurant = mock<Restaurant> {
            on { getId() } doReturn desk.getRestaurantId()
            on { getFoh() } doReturn foh
        }
        DeliveryService.setSimulator(mock<Simulator> { on { restaurantsById(any()) } doReturn restaurant })
    }

    @Test
    fun departedTakesTheOrderOutOfTheReadyQueue() {
        val desk = desk()
        val order = orderFor(casual(3, 1, deliveryDistance = 5))
        desk.enqueue(order)
        desk.readyOrder(order)

        desk.departed(order)

        assertTrue(desk.getReady().isEmpty())
    }

    @Test
    fun resetForEveningSendsEveryDriverHomeAccordingToItsState() {
        captureLog()
        val delivering = delivering(1, orderFor(casual(1, 1, deliveryDistance = 5)))
        val loading = loading(2, orderFor(casual(2, 2, deliveryDistance = 7)))
        val idle = waiting(3)
        val returning = returning(4)
        val desk = desk(delivering, loading, idle, returning)
        desk.enqueue(orderFor(casual(5, 1, deliveryDistance = 5)))

        desk.resetForEvening()

        assertTrue(delivering.isWaiting())
        assertNull(delivering.getId())
        assertTrue(loading.isWaiting())
        assertNull(loading.getId())
        assertTrue(idle.isWaiting())
        assertNull(idle.getId())
        assertTrue(returning.isReturning())
        assertTrue(desk.getNewOrders().isEmpty())
        assertEquals(1, desk.grantDriverId())
    }
}
