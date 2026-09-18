package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F30: the end of the evening. Everything that belongs to tonight is dropped - visits, reservations,
 * merged tables, the waiters' ids and loads and the delivery desk's state - while an EVENT booked for a
 * later evening is kept.
 */
class FrontOfTheHouseClosingTest {

    private val tables = TableAssignmentService(
        mutableListOf(Table(1, 2, TableType.COMMON), Table(2, 2, TableType.COMMON)),
    )
    private val reservations = ReservationBook(tables)
    private val waiters = listOf(Waiter(), Waiter())
    private val waitstaff = WaiterAssignmentService(waiters.toMutableList())
    private val desk = mock<DeliveryDesk>().also {
        whenever(it.getDrivers()).thenReturn(emptyList())
        whenever(it.getReady()).thenReturn(emptyList())
    }
    private val foh = FrontOfTheHouse(
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

    @Test
    fun closingTheEveningSplitsTablesDropsReservationsAndResetsTheStaff() {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        val group = regular(1, 4)
        foh.prepareEvening(1, mutableListOf(group))
        foh.callSeatingAndOrdering(listOf(group))
        assertTrue(waiters.any { it.id != null })

        foh.closeEvening()

        assertNull(reservations.claim(1))
        assertEquals(mapOf(TableType.COMMON to 4), tables.freeSeats())
        assertTrue(waiters.all { it.id == null && it.currentLoad == 0 })
        verify(desk, times(2)).resetForEvening()
    }

    @Test
    fun afterClosingTheNextEveningGrantsWaiterIdsFromOneAgain() {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        val first = regular(1, 2)
        foh.prepareEvening(1, mutableListOf(first))
        foh.callSeatingAndOrdering(listOf(first))
        foh.closeEvening()
        val second = regular(2, 2)
        foh.prepareEvening(2, mutableListOf(second))
        val log = captureLog()

        foh.callSeatingAndOrdering(listOf(second))

        assertTrue(logLines(log).any { it.endsWith("Group 2 seated at table 1 by waitstaff 1.") })
    }

    @Test
    fun anEventBookedForALaterEveningSurvivesTheEndOfTonight() {
        val later = GlobalClock.getEvening() + 3
        val group = event(3, 4, eventEvening = later)
        foh.bookEvent(group, later)

        foh.closeEvening()

        assertEquals(listOf(group), reservations.expectedFor(later))
    }
}
