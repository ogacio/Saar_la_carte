package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.simulation.BrowsingService
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration of the delivery path (F20, F24, F28, F29) with real registry, browsing, desk, driver,
 * order and customers. DeliveryService.placeOrder and DeliveryDesk.sendForOrder are not usable yet,
 * so the hand-over to the driver is done the way sendForOrder does it: the desk grants the id and the
 * driver receives the ready order.
 */
class DeliveryIntegrationTest {

    private val ratings = RatingBook.also { it.initializeRatings(811, 0, 0) }

    private fun deliveryRestaurant(drivers: Int) = RestaurantData(
        811,
        RestaurantType.EUROPEAN,
        1,
        24,
        mutableListOf(recipe(1)),
        mutableMapOf(TableType.COMMON to 0),
        drivers,
        false,
        0,
        mutableMapOf(),
    )

    @Test
    fun deliveryGroupDecidesEarlyBooksTheDriverAndReceivesItsMeals() {
        GlobalClock.advanceEvening()
        repeat(5) { GlobalClock.advanceTick() }
        val group = casual(1, 2, deliveryDistance = 7, evenings = listOf(1), visitingTick = 10)
        val snapshot = deliveryRestaurant(drivers = 1)
        val browsing = BrowsingService(mutableListOf(snapshot), ratings)
        val registry = CustomerRegistry(mutableListOf(group))

        assertEquals(listOf(1), registry.deciding(1, 5).map { it.id() })
        assertEquals(811, browsing.choose(group))
        assertEquals(0, snapshot.getFreeDrivers())
        assertNull(browsing.choose(casual(2, 1, deliveryDistance = 3)))

        val driver = DeliveryDriver(811)
        val desk = DeliveryDesk(mutableListOf(driver), 811)
        val meals = group.members().map { Meal(null, it, recipe(1), status = MealStatus.COOKED) }.toMutableList()
        val order = Order(group, 811, group.id(), 5, true, meals)
        meals.forEach { it.orderId = order.getId() }
        desk.enqueue(order)
        desk.readyOrder(order)
        driver.setId(desk.grantDriverId())
        val log = captureLog(LogLevel.DEBUG)

        driver.receiveOrder(order)
        driver.logPreparation()
        repeat(4) {
            GlobalClock.advanceTick()
            driver.plusTick()
        }
        Statistics.record(811, group.members().count { it.status() == CustomerStatus.SERVED }, delivered = true)

        assertEquals(
            listOf(
                "[INFO] Delivery Preparation (R 811): Driver 1 prepares driving order ${order.getId()} to group 1, " +
                    "which will take 2 ticks.",
                "[DEBUG] Delivery Driving (R 811): Driver 1 drove 5 km and needs 1 more ticks.",
                "[DEBUG] Delivery Driving (R 811): Driver 1 drove 2 km and needs 0 more ticks.",
                "[INFO] Delivery Arrival (R 811): Driver 1 arrived at group 1 with order ${order.getId()}.",
                "[IMPORTANT] Delivery Finished (R 811): Driver 1 gave delivery of order ${order.getId()} to group 1.",
                "[INFO] Delivery Returned (R 811): Driver 1 has returned.",
            ),
            logLines(log),
        )
        assertTrue(driver.isFree())
        val stats = captureLog(LogLevel.IMPORTANT)
        Statistics.report(listOf(811))
        assertEquals(
            "[IMPORTANT] Simulation Statistics: Restaurant 811 delivered meals to 2 customers.",
            logLines(stats)[3],
        )
    }

    @Test
    fun deskResetAtTheEndOfTheEveningAbortsTheDriverOnTheWay() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        val group = casual(1, 1, deliveryDistance = 10)
        val driver = DeliveryDriver(812)
        val desk = DeliveryDesk(mutableListOf(driver), 812)
        driver.setId(desk.grantDriverId())
        driver.receiveOrder(Order(group, 812, group.id(), 1, true, mutableListOf()))
        captureLog()

        desk.resetForEvening()

        assertTrue(driver.isWaiting())
        assertNull(driver.getId())
        assertEquals(1, desk.grantDriverId())
    }
}
