package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
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
        verify(desk, times(1)).resetForEvening()
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

    /**
     * Regression for the double `resetForEvening()` call (fixed in `d25ba0a`): a driver still
     * RETURNING when the evening closes must keep its RETURNING/id state through exactly one
     * `closeEvening()` and, once home, drop its stale id via a single `switch()` toggle. Two
     * resets (the old bug) would flip `switch` back off and the id would survive incorrectly.
     */
    @Test
    fun aReturningDriverDropsItsIdAfterExactlyOneReset() {
        val realDesk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        val driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
        realDesk.addDriver(driver)
        driver.receiveOrder(orderFor(9))
        deliverAndStartReturning(driver)

        realDesk.resetForEvening()
        arriveHome(driver)

        assertTrue(driver.isWaiting())
        assertNull(driver.getId(), "one reset must let the returning driver shed its stale id on arrival")
    }

    @Test
    fun twoResetsOnAReturningDriverWronglyKeepItsStaleId() {
        val realDesk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        val driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
        realDesk.addDriver(driver)
        driver.receiveOrder(orderFor(9))
        deliverAndStartReturning(driver)

        realDesk.resetForEvening()
        realDesk.resetForEvening()
        arriveHome(driver)

        assertEquals(
            1,
            driver.getId(),
            "documents the exact bug this pair guards against: a second resetForEvening() call " +
                "toggles switch back off, so the driver wrongly keeps id 1 into the next evening",
        )
    }

    /**
     * The reproduction of the corruption at full simulation scale: two drivers, one returning
     * across the evening boundary. Before the fix this collapses to one driver id and silently
     * drops whichever order that stale driver was holding.
     */
    @Test
    fun twoDriversStayDistinctAcrossOneEveningBoundary() {
        val realDesk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        val returning = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
        val fresh = DeliveryDriver(RESTAURANT_ID)
        realDesk.addDriver(returning)
        realDesk.addDriver(fresh)
        returning.receiveOrder(orderFor(9))
        deliverAndStartReturning(returning)

        realDesk.resetForEvening()
        arriveHome(returning)
        fresh.setId(realDesk.grantDriverId())
        fresh.receiveOrder(orderFor(10))
        captureLog()
        fresh.prepare()

        assertEquals(1, fresh.getId(), "the fresh driver must be granted a distinct id, not reuse the stale one")
        assertNull(returning.getId())
    }

    private fun orderFor(groupId: Int): Order {
        val group = casual(groupId, 1, deliveryDistance = 5)
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, groupId, GlobalClock.currentTick, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun deliverAndStartReturning(driver: DeliveryDriver) {
        captureLog()
        driver.prepare()
        GlobalClock.advanceTick()
        driver.prepare()
        driver.drive()
        driver.arrive()
        driver.deliverAccepted()
    }

    private fun arriveHome(driver: DeliveryDriver) {
        GlobalClock.advanceTick()
        driver.prepare()
        driver.returnHome()
    }
}
