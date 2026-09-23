package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F29/F30 edge paths of a delivery driver: the guards that make every phase a no-op for a driver
 * in the wrong state, partial loading (forum #6), the cumulative driving distance and its cap, and
 * the evening boundary where a returning driver keeps its id until it gets home.
 */
class F29DeliveryDriverEdgeTest {

    private lateinit var driver: DeliveryDriver

    private fun orderFor(group: CustomerGroup): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun runPhases(vararg drivers: DeliveryDriver) {
        drivers.forEach { it.prepare() }
        drivers.forEach { it.drive() }
        drivers.forEach { it.arrive() }
        drivers.forEach { it.deliverAccepted() }
        drivers.forEach { it.deliverRejected() }
        drivers.forEach { it.returnHome() }
    }

    private fun tick(vararg drivers: DeliveryDriver) {
        GlobalClock.advanceTick()
        runPhases(*drivers)
    }

    @BeforeTest
    fun freshEveningAndDriver() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
    }

    @Test
    fun aDriverStillCollectingMealsIsReservedButNotOnTheRoad() {
        val order = orderFor(casual(1, 2, deliveryDistance = 5))
        driver.assignOrder(order)

        assertTrue(driver.isLoading(), "reserved on the first meal, forum #6")
        assertFalse(driver.isFree(), "the desk must not hand it a second order")
        assertFalse(driver.hasDeparted())
        assertEquals(2, driver.pendingMeals().size)

        val log = captureLog()
        tick(driver)
        assertTrue(logLines(log).isEmpty(), "a loading driver drives nowhere")
        assertTrue(driver.isLoading())
    }

    @Test
    fun aPartlyLoadedDriverWaitsForTheRestOfTheOrder() {
        val order = orderFor(casual(1, 2, deliveryDistance = 5))
        driver.assignOrder(order)

        driver.loadMeals(driver.pendingMeals().take(1))

        assertTrue(driver.isLoading(), "half an order does not start the journey")
        assertFalse(driver.hasDeparted())
        assertEquals(1, driver.pendingMeals().size, "one meal is still with the waitstaff")

        driver.loadMeals(driver.pendingMeals())

        assertTrue(driver.isDelivering(), "the last meal starts the journey")
        assertTrue(driver.pendingMeals().isEmpty())
    }

    @Test
    fun releasingAPartLoadFreesTheDriverWithoutTouchingItsId() {
        val order = orderFor(casual(1, 2, deliveryDistance = 5))
        driver.assignOrder(order)
        driver.loadMeals(driver.pendingMeals().take(1))

        driver.releaseLoad()

        assertTrue(driver.isFree())
        assertNull(driver.currentOrder())
        assertEquals(1, driver.getId(), "the id is granted for the evening, not for the order")
    }

    @Test
    fun releaseLoadDoesNothingToADriverAlreadyOnTheRoad() {
        driver.receiveOrder(orderFor(casual(1, 2, deliveryDistance = 5)))

        driver.releaseLoad()

        assertTrue(driver.isDelivering(), "a driver that already left keeps its order")
    }

    @Test
    fun theDrivenDistanceAddsUpAndIsCappedAtTheDeliveryDistance() {
        driver.receiveOrder(orderFor(casual(1, 1, deliveryDistance = 12)))
        val log = captureLog()

        tick(driver) // 5 km
        tick(driver) // 10 km
        tick(driver) // 12 km, capped, and arrival

        val driving = logLines(log).filter { it.contains("Delivery Driving") }
        assertEquals(3, driving.size)
        assertTrue(driving[0].contains("drove 5 km and needs 2 more ticks"), driving[0])
        assertTrue(driving[1].contains("drove 10 km and needs 1 more ticks"), driving[1])
        assertTrue(driving[2].contains("drove 12 km and needs 0 more ticks"), driving[2])
    }

    @Test
    fun anExactMultipleOfTheTickDistanceEndsOnTheTotal() {
        driver.receiveOrder(orderFor(casual(1, 1, deliveryDistance = 10)))
        val log = captureLog()

        tick(driver)
        tick(driver)

        val driving = logLines(log).filter { it.contains("Delivery Driving") }
        assertEquals(2, driving.size)
        assertTrue(driving[1].contains("drove 10 km"), driving[1])
    }

    @Test
    fun abortDropsTheIdWhileClearingAfterAReturnKeepsIt() {
        driver.receiveOrder(orderFor(casual(1, 1, deliveryDistance = 5)))

        driver.abort()

        assertTrue(driver.isFree())
        assertNull(driver.getId(), "an aborted driver is unknown again")
        assertNull(driver.currentOrder())
    }

    @Test
    fun aDriverReturningOverTheEveningBoundaryShedsItsIdOnlyOnceHome() {
        driver.receiveOrder(orderFor(casual(1, 1, deliveryDistance = 10)))
        tick(driver)
        tick(driver) // arrives and starts the two-tick return

        assertTrue(driver.isReturning())
        driver.switch() // what DeliveryDesk.resetForEvening does at the boundary

        GlobalClock.advanceEvening()
        val log = captureLog()
        tick(driver)
        tick(driver)

        assertTrue(logLines(log).any { it.contains("Delivery Returned") }, "it still drives home")
        assertTrue(driver.isFree())
        assertNull(driver.getId(), "the stale id is dropped only once it is back")
    }

    @Test
    fun logPreparationIsSilentForADriverWithoutAnOrder() {
        val log = captureLog()

        driver.logPreparation()

        assertTrue(logLines(log).isEmpty())
    }

    @Test
    fun takeResolvedOrderIsConsumedOnRead() {
        driver.receiveOrder(orderFor(casual(1, 1, deliveryDistance = 5)))
        tick(driver)

        val first = driver.takeResolvedOrder()
        assertEquals(false, first?.second, "delivered, not given up")
        assertNull(driver.takeResolvedOrder(), "the desk books each resolution once")
    }
}
