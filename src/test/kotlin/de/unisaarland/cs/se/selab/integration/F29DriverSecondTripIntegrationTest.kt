package de.unisaarland.cs.se.selab.integration

/* F29/F20: one driver serving two delivery orders in one evening, through the real serving step. */

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.foh.FohServices
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * "For deliveries, the meals queue until all meals of an order are ready and a driver is free." With
 * a single driver the second ready order waits at the desk while the driver is out and on its way
 * back, and is handed over in the first tick the driver is home again. The driver keeps the id it
 * got on its first trip: ids are granted once per evening, so both preparations name driver 1.
 */
class F29DriverSecondTripIntegrationTest {

    private lateinit var desk: DeliveryDesk
    private lateinit var driver: DeliveryDriver
    private lateinit var foh: FrontOfTheHouse

    @BeforeTest
    fun oneDriverOneWaiterAtTickTwo() {
        GlobalClock.advanceEvening()
        repeat(2) { GlobalClock.advanceTick() }
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        driver = DeliveryDriver(RESTAURANT_ID)
        desk = DeliveryDesk(mutableListOf(driver), RESTAURANT_ID)
        val tables = TableAssignmentService(mutableListOf())
        val reservations = ReservationBook(tables)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val services = FohServices(
            SeatingService(tables, waitstaff, reservations),
            OrderingService(waitstaff, RestaurantType.EUROPEAN),
            ServingService(waitstaff, desk, RestaurantType.EUROPEAN),
            DiningService(),
            EscortingService(waitstaff),
            RatingService(),
        )
        val sbu = SubUnits(RESTAURANT_ID, mock<Menu>(), mock<Pantry>(), mock<Kitchen>())
        foh = FrontOfTheHouse(sbu, tables, reservations, waitstaff, services, desk)
    }

    /** A cooked delivery order of [group], waiting at the desk for a driver. */
    private fun readyOrderOf(group: CustomerGroup): Order {
        val meals = group.members()
            .map { Meal(null, it, recipe(1), status = MealStatus.COOKED) }
            .toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), GlobalClock.currentTick, true, meals)
        meals.forEach { it.orderId = order.getId() }
        group.orderPlaced()
        desk.enqueue(order)
        desk.readyOrder(order)
        return order
    }

    /** Serving and the delivery phases of one tick, in the order of Restaurant.runRestaurantTick. */
    private fun tick() {
        GlobalClock.advanceTick()
        foh.beginTick()
        foh.callServingService()
        foh.callDeliveryDesk()
        foh.callDiningService()
        foh.callRatingService()
    }

    /** The preparation line of [order] driven by driver 1; 5 km are one tick of travel. */
    private fun preparationByDriverOne(order: Order) =
        "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order ${order.getId()} " +
            "to group ${order.getCustomerGroupId()}, which will take 1 ticks."

    @Test
    fun theSecondOrderWaitsForTheDriverAndLeavesUnderTheSameDriverId() {
        val first = readyOrderOf(casual(1, 1, deliveryDistance = 5, visitingTick = 20))
        val second = readyOrderOf(casual(2, 1, deliveryDistance = 5, visitingTick = 20))
        val log = captureLog()

        tick() // the first order is handed over and prepared
        tick() // driven and delivered
        assertTrue(second in desk.getReady(), "the only driver is out, so the second order waits")
        tick() // the driver drives back
        tick() // the second order is handed over to the same driver

        val lines = logLines(log)
        val preparations = lines.filter { it.startsWith("[INFO] Delivery Preparation") }
        assertEquals(listOf(preparationByDriverOne(first), preparationByDriverOne(second)), preparations)
        val returned = lines.indexOf("[INFO] Delivery Returned (R 1): Driver 1 has returned.")
        assertTrue(returned in 0 until lines.indexOf(preparations[1]), "the second trip starts after the return")
        assertTrue(desk.getReady().isEmpty())
        assertEquals(2, desk.grantDriverId(), "only one driver id was granted this evening")
    }
}
