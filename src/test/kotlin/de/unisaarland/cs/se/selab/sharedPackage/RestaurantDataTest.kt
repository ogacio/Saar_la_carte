package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F28: the browsing snapshot of one restaurant. */
class RestaurantDataTest {

    private fun data(
        freeSeats: MutableMap<TableType, Int> = mutableMapOf(TableType.COMMON to 6),
        freeDrivers: Int = 1,
        totalSeats: Int = 10,
    ) = RestaurantData(
        id = 2,
        type = RestaurantType.ASIAN,
        openingTick = 3,
        closingTick = 5,
        dishes = mutableListOf(recipe(1)),
        freeSeatsStatic = freeSeats,
        freeDrivers = freeDrivers,
        hostsEvents = true,
        totalSeats = totalSeats,
        eventSeatsBookedStatic = mutableMapOf(),
    )

    @Test
    fun gettersReturnTheSnapshotValues() {
        val snapshot = data()

        assertEquals(2, snapshot.getId())
        assertEquals(RestaurantType.ASIAN, snapshot.getType())
        assertEquals(3, snapshot.getOpeningTick())
        assertEquals(5, snapshot.getClosingTick())
        assertTrue(snapshot.getHostsEvents())
        assertEquals(listOf(1), snapshot.getDishes().map { it.id })
        assertEquals(1, snapshot.getFreeDrivers())
    }

    @Test
    fun eatInGroupTakesSeatsOfItsTableType() {
        val snapshot = data()

        snapshot.take(casual(1, 4))

        assertEquals(2, snapshot.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun eatInGroupTakingTooManySeatsFails() {
        val snapshot = data()

        assertFailsWith<IllegalArgumentException> { snapshot.take(casual(1, 7)) }
    }

    @Test
    fun eatInGroupWithoutSeatsOfItsTypeFails() {
        val snapshot = data()

        assertFailsWith<IllegalArgumentException> { snapshot.take(casual(1, 1, tableType = TableType.BAR)) }
    }

    @Test
    fun deliveryGroupTakesADriverNotSeats() {
        val snapshot = data()

        snapshot.take(casual(1, 4, deliveryDistance = 3))

        assertEquals(0, snapshot.getFreeDrivers())
        assertEquals(6, snapshot.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun deliveryGroupWithoutFreeDriverFails() {
        val snapshot = data(freeDrivers = 0)

        assertFailsWith<IllegalArgumentException> { snapshot.take(casual(1, 4, deliveryDistance = 3)) }
    }

    @Test
    fun eventSeatsAreCountedPerEvening() {
        val snapshot = data(totalSeats = 10)

        snapshot.takeForEvent(event(1, 4, eventEvening = 5))
        snapshot.takeForEvent(event(2, 3, eventEvening = 5))

        assertEquals(3, snapshot.eventSeatsLeft(5))
        assertEquals(10, snapshot.eventSeatsLeft(6))
    }

    @Test
    fun openAtIncludesOpeningAndClosingTick() {
        val snapshot = data()

        assertFalse(snapshot.openAt(2))
        assertTrue(snapshot.openAt(3))
        assertTrue(snapshot.openAt(5))
        assertFalse(snapshot.openAt(6))
    }
}
