package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.simulation.BrowsingService
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** F20: the delivery desk's order queues, driver ids, free drivers, hand-over to a driver and evening reset. */
class DeliveryDeskTest {

    private fun desk(vararg drivers: DeliveryDriver) = DeliveryDesk(drivers.toMutableList(), RESTAURANT_ID)

    @Test
    fun enqueuedOrdersWaitAsNewOrders() {
        val desk = desk()
        val order = mock<Order>()

        desk.enqueue(order)

        assertEquals(listOf(order), desk.getNewOrders())
        assertTrue(desk.getReady().isEmpty())
        assertEquals(RESTAURANT_ID, desk.getRestaurantId())
    }

    @Test
    fun readyOrderMovesFromNewToReady() {
        val desk = desk()
        val first = mock<Order>()
        val second = mock<Order>()
        desk.enqueue(first)
        desk.enqueue(second)

        desk.readyOrder(first)

        assertEquals(listOf(second), desk.getNewOrders())
        assertEquals(listOf(first), desk.getReady())
    }

    @Test
    fun driverIdsAreGrantedFromOneUpwards() {
        val desk = desk()

        assertEquals(listOf(1, 2, 3), List(3) { desk.grantDriverId() })
    }

    @Test
    fun resetForEveningHandlesEachDriverStateAndRestartsIds() {
        val delivering = mock<DeliveryDriver> { on { isDelivering() } doReturn true }
        val waiting = mock<DeliveryDriver> { on { isWaiting() } doReturn true }
        val returning = mock<DeliveryDriver> { on { isReturning() } doReturn true }
        val unknown = mock<DeliveryDriver>()
        val desk = desk(delivering, waiting, returning, unknown)
        desk.enqueue(mock())
        desk.grantDriverId()
        desk.grantDriverId()

        desk.resetForEvening()

        verify(delivering).abort()
        verify(waiting).resetId()
        verify(waiting, never()).abort()
        verify(returning).switch()
        verify(returning, never()).abort()
        verify(unknown, never()).abort()
        verify(unknown, never()).resetId()
        verify(unknown, never()).switch()
        assertTrue(desk.getNewOrders().isEmpty())
        assertTrue(desk.getReady().isEmpty())
        assertEquals(1, desk.grantDriverId())
    }

    @Test
    fun onlyWaitingDriversCountAsFree() {
        val free = mock<DeliveryDriver> { on { isFree() } doReturn true }
        val busy = mock<DeliveryDriver> { on { isFree() } doReturn false }

        assertEquals(1, desk(free, busy).amountFreeDrivers())
        assertEquals(0, desk().amountFreeDrivers())
    }

    @Test
    fun driversCanJoinAndLeaveTheDesk() {
        val first = DeliveryDriver(RESTAURANT_ID)
        val second = DeliveryDriver(RESTAURANT_ID)
        val desk = desk(first)

        desk.addDriver(second)
        assertEquals(listOf(first, second), desk.getDrivers())

        desk.removeDriver(first)
        assertEquals(listOf(second), desk.getDrivers())
    }

    @Test
    fun aReadyOrderStaysReadyWhenNoDriverIsFree() {
        val desk = DeliveryDesk(mutableListOf(), NO_DRIVER_RESTAURANT)
        registerWithDeliveryService(desk)
        val order = mock<Order>()
        desk.enqueue(order)
        desk.readyOrder(order)

        assertNull(desk.sendForOrder(order))
        assertEquals(listOf(order), desk.getReady())
    }

    @Test
    fun aFreeDriverTakesTheReadyOrderAndReceivesTheFirstIdOfTheEvening() {
        val desk = DeliveryDesk(mutableListOf(), DRIVER_RESTAURANT)
        registerWithDeliveryService(desk)
        DeliveryService.addDriver(DRIVER_RESTAURANT)
        val order = Order(casual(4, 2, deliveryDistance = 7), DRIVER_RESTAURANT, 4, 1, true, mutableListOf())
        desk.enqueue(order)
        desk.readyOrder(order)
        captureLog()

        assertEquals(1, desk.sendForOrder(order))

        val driver = desk.getDrivers().single()
        assertTrue(desk.getReady().isEmpty())
        assertTrue(driver.isDelivering())
        assertEquals(0, desk.amountFreeDrivers())
        DeliveryService.rmDriver(DRIVER_RESTAURANT)
    }

    // BUG (DeliveryDesk.sendForOrder / DeliveryService.chooseDriverForOrder, Constantin): the drivers of the
    // restaurants file live only in the desk, while the driver is chosen from DeliveryService's own list,
    // which only DeliveryService.addDriver fills. A desk built by the RestaurantParser therefore never sends
    // a delivery out. Uncomment once the desk chooses among its own drivers.
    // @Test
    // fun aDriverFromTheRestaurantsFileTakesTheReadyOrder() {
    //     val restaurantId = 963
    //     val driver = DeliveryDriver(restaurantId)
    //     val desk = DeliveryDesk(mutableListOf(driver), restaurantId)
    //     val order = Order(casual(4, 2, deliveryDistance = 7), restaurantId, 4, 1, true, mutableListOf())
    //     desk.enqueue(order)
    //     desk.readyOrder(order)
    //     captureLog()
    //
    //     assertEquals(1, desk.sendForOrder(order))
    //     assertTrue(driver.isDelivering())
    // }

    /** Lets the delivery service find [desk] through a simulator whose only restaurant owns it. */
    private fun registerWithDeliveryService(desk: DeliveryDesk) {
        val foh = mock<FrontOfTheHouse> { on { getDeliveryDesk() } doReturn desk }
        val restaurant = mock<Restaurant> {
            on { getId() } doReturn desk.getRestaurantId()
            on { getFoh() } doReturn foh
        }
        DeliveryService.setSimulator(
            Simulator(
                0,
                mutableListOf(restaurant),
                CustomerRegistry(mutableListOf()),
                mutableListOf(),
                BrowsingService(mutableListOf(), RatingBook),
            ),
        )
    }

    /** Restaurant ids no other test registers drivers for, because the delivery service is a singleton. */
    private companion object {
        const val NO_DRIVER_RESTAURANT = 961
        const val DRIVER_RESTAURANT = 962
    }
}
