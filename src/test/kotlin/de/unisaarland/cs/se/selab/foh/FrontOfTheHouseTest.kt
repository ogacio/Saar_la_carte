package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The facade of the front of house at the edges of an evening: the preparation reserves the tables of
 * the REGULAR and EVENT groups, and at the end of the opening time "the front of house is immediately
 * cleaned and tables are separated" and "reservation from this evening are discarded", while the groups
 * that were sent out still rate in that same tick.
 */
class FrontOfTheHouseTest {

    private val sbu = subUnits()

    /** A front of house with [tableSizes] COMMON tables, one waiter and a desk without drivers. */
    private class Floor(vararg tableSizes: Int) {
        val tables = TableAssignmentService(
            tableSizes.mapIndexed { index, size -> Table(index + 1, size, TableType.COMMON) }.toMutableList(),
        )
        val reservations = ReservationBook(tables)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val desk = mock<DeliveryDesk>().also {
            whenever(it.getDrivers()).thenReturn(emptyList())
            whenever(it.getReady()).thenReturn(emptyList())
        }
        val services = FohServices(
            SeatingService(tables, waitstaff, reservations),
            OrderingService(waitstaff, RestaurantType.EUROPEAN),
            ServingService(waitstaff, desk, RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(waitstaff),
            RatingService(),
        )

        fun foh(sbu: de.unisaarland.cs.se.selab.simulation.SubUnits) =
            FrontOfTheHouse(sbu, tables, reservations, waitstaff, services, desk)
    }

    private fun prepared(floor: Floor, group: CustomerGroup, evening: Int = 1): FrontOfTheHouse {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        val foh = floor.foh(sbu)
        foh.prepareEvening(evening, mutableListOf(group))
        return foh
    }

    @Test
    fun preparationReservesATableForARegularGroup() {
        val floor = Floor(2, 2)
        val group = regular(1, 4)

        prepared(floor, group)

        // The group of 4 gets both tables merged, so no free seats are left.
        assertNotNull(floor.reservations.claim(1))
        assertTrue(floor.tables.freeSeats().values.sum() == 0)
    }

    @Test
    fun atTheEndOfTheOpeningTimeTheFloorIsCleanedAfterTheRating() {
        val floor = Floor(2, 2)
        val group = regular(1, 4)
        val foh = prepared(floor, group)
        foh.callSeatingAndOrdering(listOf(group))

        foh.closeOpeningTime()
        foh.callRatingService()

        // Reservations are discarded and the merged table is split again, so every seat is free.
        assertNull(floor.reservations.claim(1))
        assertEquals(4, floor.tables.freeSeats().values.sum())
        assertEquals(4, floor.tables.totalSeats())
    }

    @Test
    fun groupsSentOutAtTheEndOfTheOpeningTimeRateInTheSameTick() {
        val floor = Floor(2, 2)
        val group = regular(1, 4)
        val foh = prepared(floor, group)
        foh.callSeatingAndOrdering(listOf(group))
        val log = captureLog()

        foh.closeOpeningTime()
        foh.callRatingService()

        assertTrue(logLines(log).any { it.contains("Group 1 rates the restaurant 1 with NEGATIVE rating") })
        assertTrue(logLines(log).any { it.contains("Rating Status (R 1): 1 groups performed ratings this tick.") })
    }

    @Test
    fun aGroupWithoutAReservedTableIsSentAwayAndRatesInTheFirstTick() {
        val floor = Floor(2)
        val lucky = regular(1, 2)
        val unlucky = regular(2, 2)
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        val foh = floor.foh(sbu)
        val log = captureLog()

        foh.prepareEvening(1, mutableListOf(lucky, unlucky))
        foh.callRatingService()

        val noReserving = "[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 2."
        assertTrue(logLines(log).any { it == noReserving })
        assertTrue(logLines(log).any { it.contains("Group 2 rates the restaurant 1 with NEGATIVE rating") })
    }
}
