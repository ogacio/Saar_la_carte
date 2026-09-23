package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F29/F31: the delivery service's driver bookkeeping - staff-change incidents for drivers, handing the
 * drivers of the restaurants file to the desks, and picking a free driver for an order.
 *
 * [DeliveryService] is a singleton whose driver list other tests also fill, so every test uses its own
 * restaurant id, removes the drivers it added, and resolves any restaurant id to its own desk (a
 * leftover driver of another test then lands on this desk instead of failing the lookup).
 */
class DeliveryServiceDriversTest {

    private fun deskFor(restaurantId: Int, vararg drivers: DeliveryDriver): DeliveryDesk {
        val desk = DeliveryDesk(drivers.toMutableList(), restaurantId)
        val foh = mock<FrontOfTheHouse> { on { getDeliveryDesk() } doReturn desk }
        val restaurant = mock<Restaurant> {
            on { getId() } doReturn restaurantId
            on { getFoh() } doReturn foh
        }
        val simulator = mock<Simulator> { on { restaurantsById(any()) } doReturn restaurant }
        DeliveryService.setSimulator(simulator)
        return desk
    }

    private fun removeAllDriversOf(restaurantId: Int, desk: DeliveryDesk) {
        repeat(desk.getDrivers().count { it.getRestaurantId() == restaurantId }) {
            DeliveryService.rmDriver(restaurantId)
        }
    }

    @Test
    fun hiringDriversAddsThemToTheDeskAndFiringRemovesThem() {
        val desk = deskFor(HIRE_RESTAURANT)

        DeliveryService.changeStaff(HIRE_RESTAURANT, 2)
        assertEquals(2, desk.getDrivers().size)
        assertEquals(2, desk.amountFreeDrivers())

        DeliveryService.changeStaff(HIRE_RESTAURANT, -1)
        assertEquals(1, desk.getDrivers().size)

        DeliveryService.changeStaff(HIRE_RESTAURANT, 0)
        assertEquals(1, desk.getDrivers().size)

        removeAllDriversOf(HIRE_RESTAURANT, desk)
        assertTrue(desk.getDrivers().isEmpty())
    }

    @Test
    fun firingADriverOfARestaurantWithoutDriversChangesNothing() {
        val desk = deskFor(EMPTY_RESTAURANT)

        DeliveryService.changeStaff(EMPTY_RESTAURANT, -3)

        assertTrue(desk.getDrivers().isEmpty())
    }

    @Test
    fun theDriversOfTheRestaurantsFileAreHandedToTheirDesk() {
        val desk = deskFor(DISTRIBUTE_RESTAURANT)
        val driver = DeliveryDriver(DISTRIBUTE_RESTAURANT)
        DeliveryService.setAllDrivers(listOf(driver))

        DeliveryService.distributeDriversToDesks()

        assertTrue(driver in desk.getDrivers())
        removeAllDriversOf(DISTRIBUTE_RESTAURANT, desk)
    }

    @Test
    fun aFreeDriverWithoutAnIdIsGrantedTheFirstIdWhenChosen() {
        val driver = DeliveryDriver(CHOOSE_RESTAURANT)
        deskFor(CHOOSE_RESTAURANT, driver)

        assertEquals(1, DeliveryService.chooseDriverForOrder(CHOOSE_RESTAURANT))
        assertEquals(1, driver.getId())
    }

    @Test
    fun noDriverIsChosenWhenNoneIsFree() {
        deskFor(NO_FREE_RESTAURANT)

        assertNull(DeliveryService.chooseDriverForOrder(NO_FREE_RESTAURANT))
    }

    /** A simulator that knows no restaurant at all. */
    private fun simulatorWithoutRestaurants() {
        DeliveryService.setSimulator(mock<Simulator>())
    }

    @Test
    fun choosingADriverForAnUnknownRestaurantFailsLoudly() {
        simulatorWithoutRestaurants()

        assertFailsWith<IllegalStateException> { DeliveryService.chooseDriverForOrder(UNKNOWN_RESTAURANT) }
    }

    @Test
    fun hiringADriverForAnUnknownRestaurantFailsLoudly() {
        simulatorWithoutRestaurants()
        try {
            assertFailsWith<IllegalStateException> { DeliveryService.addDriver(UNKNOWN_HIRE_RESTAURANT) }
        } finally {
            // addDriver registered the driver before the lookup failed, so remove it again.
            deskFor(UNKNOWN_HIRE_RESTAURANT)
            DeliveryService.rmDriver(UNKNOWN_HIRE_RESTAURANT)
        }
    }

    @Test
    fun firingADriverOfAnUnknownRestaurantFailsLoudly() {
        DeliveryService.setAllDrivers(listOf(DeliveryDriver(UNKNOWN_FIRE_RESTAURANT)))
        simulatorWithoutRestaurants()

        // rmDriver drops the driver from its list before the lookup fails, so nothing is left behind.
        assertFailsWith<IllegalStateException> { DeliveryService.rmDriver(UNKNOWN_FIRE_RESTAURANT) }
    }

    @Test
    fun distributingADriverOfAnUnknownRestaurantFailsLoudly() {
        DeliveryService.setAllDrivers(listOf(DeliveryDriver(UNKNOWN_DISTRIBUTE_RESTAURANT)))
        simulatorWithoutRestaurants()
        try {
            assertFailsWith<IllegalStateException> { DeliveryService.distributeDriversToDesks() }
        } finally {
            deskFor(UNKNOWN_DISTRIBUTE_RESTAURANT)
            DeliveryService.rmDriver(UNKNOWN_DISTRIBUTE_RESTAURANT)
        }
    }

    private companion object {
        const val HIRE_RESTAURANT = 1171
        const val EMPTY_RESTAURANT = 1172
        const val DISTRIBUTE_RESTAURANT = 1173
        const val CHOOSE_RESTAURANT = 1174
        const val NO_FREE_RESTAURANT = 1175
        const val UNKNOWN_RESTAURANT = 1176
        const val UNKNOWN_HIRE_RESTAURANT = 1177
        const val UNKNOWN_FIRE_RESTAURANT = 1178
        const val UNKNOWN_DISTRIBUTE_RESTAURANT = 1179
    }
}
