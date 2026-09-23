package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * F24: while a restaurant is open, its tick hands the front of house the step that places a delivery
 * group's order. The step reports how many meals were ordered, and 0 when no dish could be ordered.
 */
class RestaurantDeliveryOrderTest {

    private fun restaurant(foh: FrontOfTheHouse, menu: Menu): Restaurant {
        val data = RestaurantData(
            1,
            RestaurantType.EUROPEAN,
            1,
            10,
            emptyList(),
            mapOf(TableType.COMMON to 4),
            0,
            false,
            4,
            mapOf(),
        )
        return Restaurant("Test", false, 0, 0, foh, mock<Kitchen>(), mock<Pantry>(), menu, data)
    }

    /** Runs one open tick and returns the delivery step the restaurant handed to [foh]. */
    private fun deliveryStepOf(foh: FrontOfTheHouse, menu: Menu): (CustomerGroup) -> Int {
        val restaurant = restaurant(foh, menu)
        GlobalClock.advanceEvening()
        repeat(3) { GlobalClock.advanceTick() }

        restaurant.runRestaurantTick(emptyList(), 3)

        val step = argumentCaptor<(CustomerGroup) -> Int>()
        verify(foh).callSeatingAndOrdering(any(), any(), any(), step.capture())
        return step.firstValue
    }

    @Test
    fun aDeliveryWithoutAnOrderableDishOrdersNoMeals() {
        captureLog()
        val menu = mock<Menu> { on { getOrderables() } doReturn emptyList() }
        val placeDelivery = deliveryStepOf(mock<FrontOfTheHouse>(), menu)

        assertEquals(0, placeDelivery(casual(7, 2, deliveryDistance = 5)))
    }

    @Test
    fun aPlacedDeliveryOrdersOneMealPerCustomerAndQueuesItAtTheDesk() {
        captureLog()
        val desk = DeliveryDesk(mutableListOf(), 1)
        val foh = mock<FrontOfTheHouse> { on { getDeliveryDesk() } doReturn desk }
        val menu = mock<Menu> { on { getOrderables() } doReturn listOf(recipe(1)) }
        val placeDelivery = deliveryStepOf(foh, menu)

        assertEquals(2, placeDelivery(casual(8, 2, deliveryDistance = 5)))
        assertEquals(2, desk.getNewOrders().single().getMeals().size)
    }
}
