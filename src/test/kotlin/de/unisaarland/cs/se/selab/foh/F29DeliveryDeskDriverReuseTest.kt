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
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * F29: a driver keeps the id it was granted for the whole evening, so a driver back from its first
 * trip takes the next order under the same id and the desk grants no new one. Dropping a given-up
 * order only releases the driver that was loading exactly that order, and a driver whose group is
 * still waiting is never reported as a failed delivery.
 */
class F29DeliveryDeskDriverReuseTest {

    private fun orderFor(group: CustomerGroup): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    /** One tick of the delivery phases for [driver], in the order FrontOfTheHouse.callDeliveryDesk runs them. */
    private fun tick(driver: DeliveryDriver) {
        GlobalClock.advanceTick()
        driver.prepare()
        driver.drive()
        driver.arrive()
        driver.deliverAccepted()
        driver.deliverRejected()
        driver.returnHome()
    }

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun aDriverBackFromItsFirstTripTakesTheNextOrderUnderTheSameId() {
        val driver = DeliveryDriver(RESTAURANT_ID)
        val desk = DeliveryDesk(mutableListOf(driver), RESTAURANT_ID)
        assertEquals(1, desk.sendForOrder(orderFor(casual(1, 1, deliveryDistance = 5))))

        tick(driver) // drives the 5 km, arrives and delivers
        tick(driver) // drives back
        assertTrue(driver.isFree(), "one tick out, one tick back")

        val second = orderFor(casual(2, 1, deliveryDistance = 5))
        assertSame(driver, desk.driverFor(second))
        assertEquals(1, driver.getId(), "the id is granted once per evening, not per order")
        assertEquals(2, desk.grantDriverId(), "no id was spent on the second order")
    }

    @Test
    fun droppingAnOrderLeavesADriverLoadingAnotherOrderAlone() {
        val loadingOther = DeliveryDriver(RESTAURANT_ID)
        val desk = DeliveryDesk(mutableListOf(loadingOther), RESTAURANT_ID)
        val kept = orderFor(casual(1, 2, deliveryDistance = 5))
        val dropped = orderFor(casual(2, 2, deliveryDistance = 5))
        desk.driverFor(kept)
        desk.enqueue(dropped)

        desk.drop(dropped)

        assertTrue(loadingOther.isLoading(), "it still collects the meals of the other order")
        assertSame(kept, loadingOther.currentOrder())
        assertTrue(desk.getNewOrders().isEmpty())
    }

    @Test
    fun aDriverWhoseGroupStillWaitsIsNeverReportedAsFailed() {
        val driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
        val group = casual(1, 1, deliveryDistance = 5)
        driver.receiveOrder(orderFor(group))
        GlobalClock.advanceTick()
        driver.prepare()
        driver.drive()
        val log = captureLog()

        driver.deliverRejected()

        assertTrue(logLines(log).none { it.contains("Delivery Failed") })
        assertTrue(driver.isDelivering(), "the rejected phase leaves a welcome delivery to the finished phase")

        driver.deliverAccepted()

        assertTrue(logLines(log).any { it.contains("Delivery Finished") })
        assertTrue(driver.isReturning())
    }
}
