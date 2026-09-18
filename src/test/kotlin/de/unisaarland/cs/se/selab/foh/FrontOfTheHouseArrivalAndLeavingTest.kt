package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F14, F16, F30: who the front of house lets in and what a group leaves behind. Groups turned away in the
 * preparation never arrive, a group without a free waiter tries once more without arriving again, and when a
 * group leaves only a CASUAL table becomes free again. The menu is empty, so every seated group leaves at
 * once without ordering.
 */
class FrontOfTheHouseArrivalAndLeavingTest {

    private val tables = TableAssignmentService(
        mutableListOf(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON)),
    )
    private val reservations = ReservationBook(tables)
    private val desk = mock<DeliveryDesk>().also {
        whenever(it.getDrivers()).thenReturn(emptyList())
        whenever(it.getReady()).thenReturn(emptyList())
        whenever(it.getNewOrders()).thenReturn(emptyList())
    }

    private fun frontOfHouse(waiterCount: Int): FrontOfTheHouse {
        val waitstaff = WaiterAssignmentService(MutableList(waiterCount) { Waiter() })
        return FrontOfTheHouse(
            subUnits(),
            tables,
            reservations,
            waitstaff,
            FohServices(
                SeatingService(tables, waitstaff, reservations),
                OrderingService(waitstaff, RestaurantType.EUROPEAN),
                ServingService(waitstaff, desk, RestaurantType.EUROPEAN),
                DiningService(),
                EscortingService(waitstaff),
                RatingService(),
            ),
            desk,
        )
    }

    @BeforeTest
    fun freshRatings() {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
    }

    @Test
    fun regularWithoutAReservedTableIsTurnedAwayAndDoesNotArriveWhenHandedIn() {
        val foh = frontOfHouse(2)
        val tooLarge = regular(1, 6)
        val log = captureLog()

        foh.prepareEvening(1, mutableListOf(tooLarge))
        foh.callSeatingAndOrdering(listOf(tooLarge))

        assertTrue(logLines(log).any { it.endsWith("FOH No Reserving (R 1): No table could be reserved for group 1.") })
        assertTrue(logLines(log).none { it.contains("Group 1 arrived") })
    }

    @Test
    fun eventGroupTurnedAwayLosesItsBooking() {
        val foh = frontOfHouse(2)
        val group = event(2, 4).also { it.book(RESTAURANT_ID) }

        foh.sendCustomersAway(listOf(group))

        assertNull(group.bookedRestaurant())
    }

    @Test
    fun groupWithoutAFreeWaiterTriesAgainNextTickWithoutArrivingAgain() {
        val foh = frontOfHouse(0)
        val group = casual(3, 2)
        foh.prepareEvening(1, mutableListOf())
        val firstTick = captureLog()
        foh.callSeatingAndOrdering(listOf(group))
        assertTrue(logLines(firstTick).any { it.contains("Group 3 arrived") })

        val secondTick = captureLog()
        foh.callSeatingAndOrdering(emptyList())

        assertTrue(logLines(secondTick).none { it.contains("Group 3 arrived") })
        assertTrue(
            logLines(secondTick).contains("[INFO] FOH No Seating (R 1): No free waitstaff available for group 3."),
        )
    }

    @Test
    fun casualGroupThatLeavesWithoutOrderingFreesItsTable() {
        val foh = frontOfHouse(2)
        foh.prepareEvening(1, mutableListOf())
        foh.callSeatingAndOrdering(listOf(casual(4, 2)))
        assertEquals(2, tables.freeSeats()[TableType.COMMON])

        foh.callRatingService()

        assertEquals(4, tables.freeSeats()[TableType.COMMON])
    }

    @Test
    fun regularTableStaysReservedAfterTheGroupLeft() {
        val foh = frontOfHouse(2)
        val group = regular(5, 2)
        foh.prepareEvening(1, mutableListOf(group))
        foh.callSeatingAndOrdering(listOf(group))

        foh.callRatingService()

        assertEquals(2, tables.freeSeats()[TableType.COMMON])
    }

    @Test
    fun groupSentAwayForLackOfATableLeavesEveryTableFree() {
        val foh = frontOfHouse(2)
        foh.prepareEvening(1, mutableListOf())
        val log = captureLog()
        foh.callSeatingAndOrdering(listOf(casual(6, 6)))

        foh.callRatingService()

        assertTrue(logLines(log).any { it.contains("no table available, group 6 is sent away") })
        assertEquals(4, tables.freeSeats()[TableType.COMMON])
    }
}
