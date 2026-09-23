package de.unisaarland.cs.se.selab.integration

/* F24: a casual delivery group from the order to the rating, delivered or given up on. */

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
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F24DeliveryFlowTest {

    private lateinit var desk: DeliveryDesk
    private lateinit var driver: DeliveryDriver
    private lateinit var foh: FrontOfTheHouse

    /** A restaurant with one driver and one waiter; no tables, this group never comes in. */
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

    /** The delivery steps of one tick, in the order of Restaurant.runRestaurantTick. */
    private fun tick() {
        GlobalClock.advanceTick()
        foh.beginTick()
        foh.callDeliveryDesk()
        foh.callDiningService()
        foh.callRatingService()
    }

    /** A delivery order of [group], cooked and waiting at the desk. */
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

    /** A delivery order of [group] that the kitchen has not finished, so it waits at the desk. */
    private fun queuedOrderOf(group: CustomerGroup): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), GlobalClock.currentTick, true, meals)
        meals.forEach { it.orderId = order.getId() }
        group.orderPlaced()
        desk.enqueue(order)
        return order
    }

    /** Hands [order] to the driver the way the waitstaff does. */
    private fun handOver(order: Order) {
        driver.setId(desk.grantDriverId())
        driver.receiveOrder(order)
    }

    private fun startEveningAt(tick: Int) {
        GlobalClock.advanceEvening()
        repeat(tick) { GlobalClock.advanceTick() }
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
        buildRestaurant()
    }

    /** The delivered customers of this restaurant, read back out of the statistics. */
    private fun deliveredCustomers(): Int {
        val log = captureLog(LogLevel.IMPORTANT)
        Statistics.report(listOf(RESTAURANT_ID))
        val line = logLines(log).first { it.contains("delivered meals to") }
        return line.substringAfter("delivered meals to ").substringBefore(" customers").toInt()
    }

    /** A delivery that arrives before the visiting tick is finished, eaten and rated positively. */
    @Test
    fun aDeliveryThatArrivesEarlyIsEatenAndRatedPositively() {
        startEveningAt(3)
        val group = casual(1, 2, deliveryDistance = 5, visitingTick = 10)
        handOver(readyOrderOf(group))
        val before = deliveredCustomers()

        val log = captureLog()
        repeat(TICKS_TO_DELIVER_AND_EAT) { tick() }

        val lines = logLines(log)
        assertTrue(lines.any { it.contains("Delivery Finished") }, "the order never arrived: $lines")
        assertTrue(
            lines.any { it.contains("Rating (") && it.contains("POSITIVE") },
            "food before the visiting tick is a positive experience: $lines",
        )
        assertEquals(before + 2, deliveredCustomers(), "the two customers were not counted as delivered")
    }

    /**
     * "Customers with a delivery order wait until 3 ticks after the tick when they wanted it to
     * arrive, afterward they reject the delivery" (forum update 11): the group waits through tick
     * visitingTick + 3 and rejects in the tick after it. So in tick 6 it is still waiting, and the
     * Delivery Given Up line belongs to tick 7.
     */
    @Test
    fun theGroupWaitsThreeTicksAndRejectsTheDeliveryAfterThat() {
        startEveningAt(2)
        val group = casual(1, 2, deliveryDistance = 5, visitingTick = 3)
        queuedOrderOf(group)

        val waiting = captureLog()
        repeat(GIVE_UP_DELAY_TICKS) { tick() } // ticks 3, 4 and 5, still before visitingTick + 3
        assertTrue(
            logLines(waiting).none { it.contains("Delivery Given Up") },
            "the group still waits in tick 5: ${logLines(waiting)}",
        )

        val rejecting = captureLog()
        tick() // tick 6 = visitingTick + 3, the tick the group gives up in

        assertTrue(
            logLines(rejecting).any { it.contains("Delivery Given Up") },
            "the group never rejected the delivery: ${logLines(rejecting)}",
        )
    }

    /** A group that gave up rates the restaurant negatively and stops waiting. */
    @Test
    fun aGroupThatGaveUpRatesNegatively() {
        startEveningAt(2)
        val group = casual(2, 2, deliveryDistance = 5, visitingTick = 3)
        queuedOrderOf(group)

        val log = captureLog()
        repeat(GIVE_UP_DELAY_TICKS + 2) { tick() }

        val lines = logLines(log)
        assertTrue(lines.any { it.contains("Delivery Given Up") }, "nobody gave up: $lines")
        assertTrue(
            lines.any { it.contains("Rating (") && it.contains("NEGATIVE") },
            "a delivery that never came is a negative experience: $lines",
        )
    }

    /**
     * An order the group gave up on stays on the desk: "All following delivery attempts of this
     * order will fail", so a driver still collects it and reports the failure on arrival rather
     * than the order quietly disappearing.
     */
    @Test
    fun anOrderTheGroupGaveUpOnStaysOnTheDesk() {
        startEveningAt(2)
        val group = casual(3, 2, deliveryDistance = 5, visitingTick = 3)
        val order = queuedOrderOf(group)

        repeat(GIVE_UP_DELAY_TICKS + 2) { tick() }

        val stillHeld = desk.getNewOrders().any { it.getId() == order.getId() } ||
            desk.getReady().any { it.getId() == order.getId() } ||
            desk.getDrivers().any { it.currentOrder()?.getId() == order.getId() }
        assertTrue(stillHeld, "the order was withdrawn instead of being sent out to fail")
    }

    private companion object {
        /** One tick to drive, one to arrive, two to eat, one for the rating. */
        const val TICKS_TO_DELIVER_AND_EAT = 5

        /** "at the end of the 3rd tick after the tick where their food should have arrived" */
        const val GIVE_UP_DELAY_TICKS = 3
    }
}
