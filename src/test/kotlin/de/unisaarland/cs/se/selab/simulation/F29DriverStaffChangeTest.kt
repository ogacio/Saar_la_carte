package de.unisaarland.cs.se.selab.simulation

/* F29: drivers joining and leaving the restaurant while the evening runs. */

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F29DriverStaffChangeTest {

    private lateinit var desk: DeliveryDesk

    /** A restaurant the delivery service can find, with a desk of [drivers] drivers. */
    private fun restaurantWith(drivers: MutableList<DeliveryDriver>) {
        desk = DeliveryDesk(drivers, RESTAURANT_ID)
        val foh = mock<FrontOfTheHouse>()
        whenever(foh.getDeliveryDesk()).thenReturn(desk)
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getFoh()).thenReturn(foh)
        whenever(restaurant.getId()).thenReturn(RESTAURANT_ID)
        val simulator = mock<Simulator>()
        whenever(simulator.restaurantsById(RESTAURANT_ID)).thenReturn(restaurant)
        DeliveryService.setSimulator(simulator)
    }

    @BeforeTest
    fun freshRestaurant() {
        restaurantWith(mutableListOf())
    }

    /** The delivery service is a global object, so every added driver has to go again. */
    @AfterTest
    fun removeTheDriversOfThisTest() {
        repeat(desk.getDrivers().size) { DeliveryService.rmDriver(RESTAURANT_ID) }
    }

    /** A STAFFCHANGE that hires a driver puts them behind the desk of that restaurant. */
    @Test
    fun anAddedDriverJoinsTheDeskOfItsRestaurant() {
        DeliveryService.addDriver(RESTAURANT_ID)

        assertEquals(1, desk.getDrivers().size)
        assertEquals(1, desk.amountFreeDrivers(), "a new driver starts free")
    }

    /** A new driver is nameless until it drives for the first time. */
    @Test
    fun anAddedDriverHasNoIdBeforeItsFirstOrder() {
        DeliveryService.addDriver(RESTAURANT_ID)

        assertNull(desk.getDrivers().single().getId())
    }

    /** A hired driver can be chosen for an order right away and receives an id then. */
    @Test
    fun anAddedDriverCanTakeAnOrderInTheSameEvening() {
        DeliveryService.addDriver(RESTAURANT_ID)

        val id = DeliveryService.chooseDriverForOrder(RESTAURANT_ID)

        assertEquals(1, assertNotNull(id), "ids start at one per restaurant and evening")
    }

    /** A STAFFCHANGE that lets a driver go removes them from the desk as well. */
    @Test
    fun aRemovedDriverLeavesTheDesk() {
        DeliveryService.addDriver(RESTAURANT_ID)
        DeliveryService.addDriver(RESTAURANT_ID)

        DeliveryService.rmDriver(RESTAURANT_ID)

        assertEquals(1, desk.getDrivers().size, "one driver went home")
    }

    /** Without a driver the restaurant cannot deliver at all. */
    @Test
    fun aRestaurantWithoutDriversChoosesNobody() {
        DeliveryService.addDriver(RESTAURANT_ID)
        DeliveryService.rmDriver(RESTAURANT_ID)

        assertNull(DeliveryService.chooseDriverForOrder(RESTAURANT_ID))
        assertTrue(desk.getDrivers().isEmpty())
    }
}
