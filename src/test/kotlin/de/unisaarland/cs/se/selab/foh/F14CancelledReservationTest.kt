package de.unisaarland.cs.se.selab.foh

/*
 * F14 "FOH - Evening Table Reservation: reserving for regular and event tables, **canceling
 * reservations**".
 */

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F14CancelledReservationTest {

    private lateinit var tables: TableAssignmentService
    private lateinit var reservations: ReservationBook
    private lateinit var foh: FrontOfTheHouse

    /** A restaurant whose only table seats [seats]; with 0 it has no table at all. */
    private fun buildFoh(seats: Int) {
        tables = TableAssignmentService(
            if (seats == 0) mutableListOf() else mutableListOf(Table(1, seats, TableType.COMMON)),
        )
        reservations = ReservationBook(tables)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val services = FohServices(
            SeatingService(tables, waitstaff, reservations),
            OrderingService(waitstaff, RestaurantType.EUROPEAN),
            ServingService(waitstaff, DeliveryDesk(mutableListOf(), RESTAURANT_ID), RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(waitstaff),
            RatingService(),
        )
        val sbu = SubUnits(RESTAURANT_ID, mock<Menu>(), mock<Pantry>(), mock<Kitchen>())
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        foh = FrontOfTheHouse(sbu, tables, reservations, waitstaff, services, desk)
    }

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
    }

    /** The preparation reports every group it has to turn away. */
    @Test
    fun aGroupWithoutATableIsReportedInThePreparation() {
        buildFoh(seats = 0)
        val group = regular(1, 2)
        val log = captureLog()

        foh.prepareEvening(GlobalClock.getEvening(), mutableListOf(group))

        assertTrue(
            logLines(log).any { it.contains("FOH No Reserving") && it.contains("group 1") },
            "the group was turned away silently: ${logLines(log)}",
        )
    }

    /**
     * "They are told before they travel, so they never show up." Even when the simulation hands the
     * group in as an arrival, the restaurant must not greet or seat it a second time.
     */
    @Test
    fun aTurnedAwayGroupNeverArrivesAtTheDoor() {
        buildFoh(seats = 0)
        val group: CustomerGroup = regular(1, 2)
        foh.prepareEvening(GlobalClock.getEvening(), mutableListOf(group))

        GlobalClock.advanceTick()
        val log = captureLog()
        foh.beginTick()
        foh.callSeatingAndOrdering(listOf(group))

        val lines = logLines(log)
        assertTrue(lines.none { it.contains("Restaurant Arrival") }, "the group was greeted anyway: $lines")
        assertTrue(lines.none { it.contains("FOH Seating (") }, "the group was seated anyway: $lines")
    }

    /** A turned away group rates the restaurant in the first tick of the evening, negatively. */
    @Test
    fun aTurnedAwayGroupRatesInTheFirstTick() {
        buildFoh(seats = 0)
        foh.prepareEvening(GlobalClock.getEvening(), mutableListOf(regular(1, 2)))

        GlobalClock.advanceTick()
        val log = captureLog()
        foh.callRatingService()

        assertTrue(
            logLines(log).any { it.contains("Rating (") && it.contains("NEGATIVE") },
            "no negative rating for the cancelled reservation: ${logLines(log)}",
        )
    }

    /**
     * An EVENT group booked this restaurant three evenings ago. If its table cannot be reserved in
     * the evening itself, the booking is released again - the group is no longer expecting to eat
     * here, which is what the browsing service reads.
     */
    @Test
    fun anEventGroupThatLosesItsTableLosesItsBooking() {
        buildFoh(seats = 0)
        val group = event(2, 4, eventEvening = GlobalClock.getEvening())
        foh.bookEvent(group, GlobalClock.getEvening())
        group.book(RESTAURANT_ID)

        foh.prepareEvening(GlobalClock.getEvening(), mutableListOf())

        assertNull(group.bookedRestaurant(), "the group still believes it has a table")
    }
}
