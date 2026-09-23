package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
* Tests RestaurantData's seat/driver bookkeeping (take, takeForEvent) and opening-window checks.
 * No F25 browsing/ranking logic.
*/

class F25RestaurantDataTest {

    private fun dataOf(
        freeSeats: Map<TableType, Int> = mapOf(TableType.COMMON to 10),
        freeDrivers: Int = 2,
        totalSeats: Int = 20,
        eventSeatsBooked: Map<Int, Int> = emptyMap(),
        openingTick: Int = 1,
        closingTick: Int = 24,
    ) = RestaurantData(
        1, RestaurantType.EUROPEAN, openingTick, closingTick, emptyList(),
        freeSeats, freeDrivers, true, totalSeats, eventSeatsBooked,
    )

    @Test
    fun restaurantDataTakeReducesFreeSeatsOfMatchingTableType() {
        val data = dataOf(freeSeats = mapOf(TableType.COMMON to 10))
        val g: CustomerGroup = mock()
        whenever(g.isDelivery()).thenReturn(false)
        whenever(g.tableType()).thenReturn(TableType.COMMON)
        whenever(g.groupSize()).thenReturn(4)

        data.take(g)
        assertEquals(6, data.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun restaurantDataTakeThrowsWhenGroupExceedsAvailableSeats() {
        val data = dataOf(freeSeats = mapOf(TableType.COMMON to 3))
        val g: CustomerGroup = mock()
        whenever(g.isDelivery()).thenReturn(false)
        whenever(g.tableType()).thenReturn(TableType.COMMON)
        whenever(g.groupSize()).thenReturn(4)

        assertThrows(IllegalArgumentException::class.java) { data.take(g) }
    }

    @Test
    fun restaurantDataTakeForDeliveryDecrementsFreeDriversNotSeats() {
        val data = dataOf(freeSeats = mapOf(TableType.COMMON to 10), freeDrivers = 2)
        val g: CustomerGroup = mock()
        whenever(g.isDelivery()).thenReturn(true)

        data.take(g)
        assertEquals(1, data.getFreeDrivers())
        assertEquals(10, data.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun restaurantDataTakeForDeliveryThrowsWithNoFreeDriver() {
        val data = dataOf(freeDrivers = 0)
        val g: CustomerGroup = mock()
        whenever(g.isDelivery()).thenReturn(true)

        assertThrows(IllegalArgumentException::class.java) { data.take(g) }
    }

    @Test
    fun restaurantDataTakeForEventAccumulatesBookedSeatsAcrossGroups() {
        val data = dataOf(totalSeats = 50)
        val g1: CustomerGroup = mock()
        whenever(g1.getEventEvening()).thenReturn(10)
        whenever(g1.groupSize()).thenReturn(15)
        val g2: CustomerGroup = mock()
        whenever(g2.getEventEvening()).thenReturn(10)
        whenever(g2.groupSize()).thenReturn(20)

        data.takeForEvent(g1)
        data.takeForEvent(g2)
        assertEquals(50 - 35, data.eventSeatsLeft(10))
    }

    @Test
    fun restaurantDataTakeForEventOnlyBooksSeatsForTheGroupsOwnEvening() {
        val data = dataOf(totalSeats = 50)
        val g = EventCustomerGroup(
            1, 15, TableType.COMMON, 4, emptyList(), emptyList(),
            setOf(RestaurantType.EUROPEAN), 10, mapOf(RestaurantType.EUROPEAN to "Soup"),
        )

        data.takeForEvent(g)
        assertEquals(50 - 15, data.eventSeatsLeft(10))
        assertEquals(50, data.eventSeatsLeft(11))
    }

    @Test
    fun restaurantDataEventSeatsLeftDefaultsToTotalSeatsForUntouchedEvening() {
        val data = dataOf(totalSeats = 30)
        assertEquals(30, data.eventSeatsLeft(99))
    }

    @Test
    fun restaurantDataOpenAtIncludesBothOpeningAndClosingTick() {
        val data = dataOf(openingTick = 5, closingTick = 20)
        assertTrue(data.openAt(5))
        assertTrue(data.openAt(20))
        assertTrue(!data.openAt(4))
        assertTrue(!data.openAt(21))
    }

    @Test
    fun restaurantDataAcceptsNewCustomersExcludesLastThreeTicksBeforeClosing() {
        val data = dataOf(openingTick = 1, closingTick = 24)
        assertTrue(data.acceptsNewCustomersAt(21))
        assertTrue(!data.acceptsNewCustomersAt(22))
        assertTrue(!data.acceptsNewCustomersAt(24))
    }
}
