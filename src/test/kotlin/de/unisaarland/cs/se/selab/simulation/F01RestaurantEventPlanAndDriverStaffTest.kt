package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.StaffType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F01: the restaurant between the simulator and its units. At preparation an EVENT group booked for
 * tonight is always planned for by the kitchen, even though it has never ordered anywhere, while a
 * REGULAR group needs a remembered order first (forum 328). A STAFF incident of type DRIVER reaches
 * the delivery desk of this restaurant through the delivery service and never touches the kitchen.
 */
class F01RestaurantEventPlanAndDriverStaffTest {

    private val kitchen = mock<Kitchen>()
    private val menu = mock<Menu>()
    private val foh = mock<FrontOfTheHouse>()
    private val tables = TableAssignmentService(
        mutableListOf(Table(1, 4, TableType.COMMON), Table(2, 4, TableType.COMMON), Table(3, 2, TableType.COMMON)),
    )
    private val reservations = ReservationBook(tables)
    private val desk = DeliveryDesk(mutableListOf(), OWN_ID)

    init {
        whenever(foh.getTables()).thenReturn(tables)
        whenever(foh.getReservationBook()).thenReturn(reservations)
        whenever(foh.getDeliveryDesk()).thenReturn(desk)
        whenever(menu.getRecipes()).thenReturn(emptyList())
    }

    private fun restaurant() = Restaurant(
        "Own", true, 0, 0, foh, kitchen, mock<Pantry>(), menu,
        RestaurantData(OWN_ID, RestaurantType.EUROPEAN, 1, 24, emptyList(), mapOf(), 0, true, 10, mapOf()),
    )

    /** Lets the delivery service find [restaurant] by its id, as the simulator does. */
    private fun registered(restaurant: Restaurant): Restaurant {
        val simulator = mock<Simulator>()
        whenever(simulator.restaurantsById(OWN_ID)).thenReturn(restaurant)
        DeliveryService.setSimulator(simulator)
        return restaurant
    }

    /** The delivery service keeps every hired driver globally, so this test's drivers have to go again. */
    @AfterTest
    fun dismissTheDriversOfThisTest() {
        repeat(desk.getDrivers().size) { DeliveryService.rmDriver(OWN_ID) }
    }

    @Test
    fun anEventBookedForTonightIsPlannedForWithoutAnyHistory() {
        GlobalClock.advanceEvening()
        val evening = GlobalClock.getEvening()
        val booked = event(7, 4, eventEvening = evening)
        val newcomer = regular(1, 4, restaurantId = OWN_ID)
        reservations.bookAhead(booked, evening)
        val regulars = mutableListOf<CustomerGroup>(newcomer)
        reservations.openEvening(evening, regulars)

        restaurant().prepare(regulars)

        val planned = argumentCaptor<MutableList<CustomerGroup>>()
        val otherSeats = argumentCaptor<Int>()
        verify(kitchen).planEvening(planned.capture(), otherSeats.capture(), any())
        assertEquals(listOf<CustomerGroup>(booked), planned.firstValue, "the event, not the unknown regular")
        assertEquals(6, otherSeats.firstValue, "the regular's 4 seats and the 2-seat table are guessed at")
    }

    @Test
    fun aDriverStaffChangeHiresDriversForThisRestaurantsDesk() {
        val restaurant = registered(restaurant())

        restaurant.changeStaff(StaffType.DRIVER, null, 2)

        assertEquals(2, desk.getDrivers().size)
        assertTrue(desk.getDrivers().all { it.isFree() && it.getRestaurantId() == OWN_ID })
        assertEquals(2, restaurant.snapshot().getFreeDrivers(), "browsing sees the new drivers at once")
        verify(kitchen, never()).changeStaff(any(), any())
    }

    @Test
    fun aDriverStaffChangeNeverDropsBelowZeroDrivers() {
        val restaurant = registered(restaurant())
        restaurant.changeStaff(StaffType.DRIVER, null, 1)

        restaurant.changeStaff(StaffType.DRIVER, null, -3)

        assertTrue(desk.getDrivers().isEmpty())
    }

    private companion object {
        /** An id no other test uses, because the delivery service's driver list is global. */
        const val OWN_ID = 1731
    }
}
