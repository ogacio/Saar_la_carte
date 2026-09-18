package de.unisaarland.cs.se.selab.integration

/* F30: what the end of the opening time and the end of the evening do to a delivery. */

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
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F30EndOfEveningTest {

    private lateinit var desk: DeliveryDesk
    private lateinit var driver: DeliveryDriver
    private lateinit var foh: FrontOfTheHouse

    private fun buildRestaurant() {
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

    private fun startEveningAt(tick: Int) {
        GlobalClock.advanceEvening()
        repeat(tick) { GlobalClock.advanceTick() }
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        buildRestaurant()
    }

    /** Hands a cooked delivery order of [group] to the driver, the way the waitstaff does. */
    private fun sendDriverOut(group: CustomerGroup) {
        val meals = group.members()
            .map { Meal(null, it, recipe(1), status = MealStatus.COOKED) }
            .toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), GlobalClock.getTickInEvening(), true, meals)
        meals.forEach { it.orderId = order.getId() }
        group.orderPlaced()
        desk.enqueue(order)
        desk.readyOrder(order)
        driver.setId(desk.grantDriverId())
        driver.receiveOrder(order)
    }

    /**
     * "Only deliveries already given to a driver continue after the opening time of the restaurant."
     * The doors are closed while the driver is on the road; the food still reaches the customer.
     */
    @Test
    fun aDeliveryOnTheRoadStillArrivesAfterTheOpeningTimeEnded() {
        startEveningAt(3)
        sendDriverOut(casual(1, 2, deliveryDistance = 5, visitingTick = 10))

        foh.closeOpeningTime()
        val log = captureLog()
        GlobalClock.advanceTick()
        foh.callDeliveryDesk()

        assertTrue(
            logLines(log).any { it.contains("Delivery Arrival") || it.contains("Delivery Finished") },
            "the delivery stopped with the opening time: ${logLines(log)}",
        )
    }

    /**
     * "Deliveries on their way to a customer are aborted, without rating or other consequences."
     * The end of the evening catches the driver mid-drive.
     */
    @Test
    fun theEndOfTheEveningAbortsADriverOnTheWayToTheCustomer() {
        startEveningAt(3)
        sendDriverOut(casual(2, 2, deliveryDistance = 20, visitingTick = 10))

        foh.closeEvening()

        assertTrue(driver.isWaiting(), "the driver is still delivering after the evening ended")
        assertNull(driver.getId(), "the driver goes home and returns nameless tomorrow")
    }

    /**
     * "In contrast, deliveries where the driver is on the way to the restaurant continue."
     * A driver that already delivered keeps driving home when the evening ends.
     */
    @Test
    fun aDriverOnTheWayBackKeepsGoingWhenTheEveningEnds() {
        startEveningAt(3)
        sendDriverOut(casual(3, 2, deliveryDistance = 5, visitingTick = 10))
        GlobalClock.advanceTick()
        foh.callDeliveryDesk() // arrives and turns around

        foh.closeEvening()

        assertTrue(driver.isReturning(), "the driver on the way back was sent home instead of finishing")
    }

    /** The desk forgets the orders of tonight, so nothing is delivered tomorrow by accident. */
    @Test
    fun theDeskIsEmptyAfterTheEvening() {
        startEveningAt(3)
        sendDriverOut(casual(4, 2, deliveryDistance = 5, visitingTick = 10))

        foh.closeEvening()

        assertTrue(desk.getNewOrders().isEmpty(), "orders of tonight are still queued")
        assertTrue(desk.getReady().isEmpty(), "orders of tonight are still waiting for a driver")
    }
}
