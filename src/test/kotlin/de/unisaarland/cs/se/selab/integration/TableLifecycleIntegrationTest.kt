package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FohServices
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration of the table lifecycle across a full [FrontOfTheHouse]: a REGULAR/EVENT table stays
 * reserved after its group leaves ("Denkmalschutz"), while a CASUAL table - merged or not - is
 * freed and reusable right away, and a waiter's current load rises on seating and falls again
 * once its customers leave.
 */
class TableLifecycleIntegrationTest {

    private class Restaurant(id: Int, vararg tableSizes: Int) {
        val tables = TableAssignmentService(
            tableSizes.mapIndexed { index, size -> Table(index + 1, size, TableType.COMMON) }.toMutableList(),
        )
        val waiters = listOf(Waiter(), Waiter())
        val waitstaff = WaiterAssignmentService(waiters.toMutableList())
        val reservations = ReservationBook(tables)
        val menu = mock<Menu>()
        val pantry = mock<Pantry>()
        val sbu = SubUnits(id, menu, pantry, mock())
        val foh = FrontOfTheHouse(
            sbu,
            tables,
            reservations,
            waitstaff,
            FohServices(
                SeatingService(tables, waitstaff, reservations),
                OrderingService(waitstaff, RestaurantType.EUROPEAN),
                ServingService(waitstaff, DeliveryDesk(mutableListOf(), id), RestaurantType.EUROPEAN),
                DiningService(),
                EscortingService(waitstaff),
                RatingService(),
            ),
            DeliveryDesk(mutableListOf(), id),
        )

        init {
            RatingBook.initializeRatings(id, 0, 0)
        }
    }

    /** Advances the clock to the start of the next evening and returns its first global tick. */
    private fun newEvening(): Int {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        return GlobalClock.currentTick
    }

    @Test
    fun regularsReservedTableStaysBlockedAfterItLeavesSoALaterCasualIsSentAway() {
        val restaurant = Restaurant(951, 2)
        whenever(restaurant.menu.getOrderables()).thenReturn(emptyList())
        val regularGroup = regular(1, 2, restaurantId = 951)
        val casualGroup = casual(2, 2)
        restaurant.reservations.openEvening(1, listOf(regularGroup))
        newEvening()

        restaurant.foh.callSeatingAndOrdering(listOf(regularGroup))
        restaurant.foh.callRatingService()

        assertTrue(restaurant.tables.freeSeats().isEmpty())

        val log = captureLog(LogLevel.INFO)
        restaurant.foh.callSeatingAndOrdering(listOf(casualGroup))

        assertTrue(logLines(log).any { it.contains("FOH No Seating") })
    }

    @Test
    fun casualMergedTableIsFreedImmediatelyAndReMergedFreshForTheNextGroup() {
        val restaurant = Restaurant(952, 2, 2)
        whenever(restaurant.menu.getOrderables()).thenReturn(emptyList())
        val firstGroup = casual(1, 4)
        val secondGroup = casual(2, 4)
        newEvening()

        val firstLog = captureLog(LogLevel.INFO)
        restaurant.foh.callSeatingAndOrdering(listOf(firstGroup))
        restaurant.foh.callRatingService()

        assertTrue(logLines(firstLog).any { it.contains("FOH Merging Tables") })
        assertEquals(mapOf(TableType.COMMON to 4), restaurant.tables.freeSeats())

        val secondLog = captureLog(LogLevel.INFO)
        restaurant.foh.callSeatingAndOrdering(listOf(secondGroup))

        assertTrue(logLines(secondLog).any { it.contains("FOH Merging Tables") })
    }

    @Test
    fun casualGroupThatLeavesUnservedAfterFiveTicksDropsTheWaitersCurrentLoad() {
        val restaurant = Restaurant(953, 4)
        val dish = recipe(1)
        whenever(restaurant.menu.getOrderables()).thenReturn(listOf(dish))
        whenever(restaurant.pantry.reserve(dish)).thenReturn(true)
        val group = casual(1, 4)
        newEvening()

        restaurant.foh.callSeatingAndOrdering(listOf(group))
        val seatedWaiter = checkNotNull(restaurant.waiters.firstOrNull { it.currentLoad > 0 })
        assertEquals(4, seatedWaiter.currentLoad)

        repeat(5) {
            GlobalClock.advanceTick()
            restaurant.foh.callDiningService()
        }

        assertEquals(0, seatedWaiter.currentLoad)
    }
}
