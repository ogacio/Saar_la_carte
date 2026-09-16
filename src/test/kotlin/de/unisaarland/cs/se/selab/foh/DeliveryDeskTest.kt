package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** F20: the delivery desk's order queues, driver ids and evening reset. sendForOrder waits for DeliveryService. */
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
}
