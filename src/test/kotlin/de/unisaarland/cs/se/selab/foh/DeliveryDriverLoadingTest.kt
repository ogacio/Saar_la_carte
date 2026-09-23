package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F24/F29: the multi-tick LOADING phase (forum #6) that [DeliveryDriverTest] skips by using
 * [DeliveryDriver.receiveOrder]. A driver is reserved on the first meal and only departs once the
 * waitstaff has handed over every meal of the order.
 */
class DeliveryDriverLoadingTest {

    private lateinit var driver: DeliveryDriver

    private fun orderFor(group: CustomerGroup): Order {
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    @BeforeTest
    fun freshEveningAndDriver() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
    }

    @Test
    fun assignedDriverIsReservedAndLoadingWithEveryMealStillPending() {
        val order = orderFor(casual(3, 2, deliveryDistance = 7))

        driver.assignOrder(order)

        assertTrue(driver.isLoading())
        assertFalse(driver.isFree())
        assertFalse(driver.hasDeparted())
        assertEquals(order.getMeals(), driver.pendingMeals())
        assertEquals(order, driver.currentOrder())
    }

    @Test
    fun aPartlyLoadedDriverKeepsWaitingForTheRestOfTheOrder() {
        val order = orderFor(casual(3, 2, deliveryDistance = 7))
        driver.assignOrder(order)
        val firstMeal = order.getMeals().first()

        driver.loadMeals(listOf(firstMeal))

        assertTrue(driver.isLoading())
        assertFalse(driver.hasDeparted())
        assertEquals(listOf(order.getMeals()[1]), driver.pendingMeals())
    }

    @Test
    fun theLastMealStartsTheJourneyAndEmptiesThePendingMeals() {
        val order = orderFor(casual(3, 2, deliveryDistance = 7))
        driver.assignOrder(order)

        driver.loadMeals(listOf(order.getMeals().first()))
        driver.loadMeals(listOf(order.getMeals()[1]))

        assertTrue(driver.isDelivering())
        assertTrue(driver.hasDeparted())
        assertTrue(driver.pendingMeals().isEmpty())
        assertEquals(GlobalClock.getTickInEvening(), driver.getDepartureTick())
    }

    @Test
    fun releasingAPartlyLoadedOrderFreesTheDriverButKeepsItsId() {
        val order = orderFor(casual(3, 2, deliveryDistance = 7))
        driver.assignOrder(order)

        driver.releaseLoad()

        assertTrue(driver.isWaiting())
        assertTrue(driver.isFree())
        assertNull(driver.currentOrder())
        assertEquals(1, driver.getId())
    }

    @Test
    fun releaseLoadDoesNothingForADriverThatIsNotLoading() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))

        driver.releaseLoad()

        // It has already departed, so the release must not touch its delivery.
        assertTrue(driver.isDelivering())
    }

    @Test
    fun aDeliveryTheGroupGaveUpWaitingForFailsOnArrival() {
        val gaveUpOnDelivery = mock<CustomerGroup> {
            on { id() } doReturn 3
            on { deliveryWasGivenUp() } doReturn true
            on { getDeliveryDistance() } doReturn 5
            on { members() } doReturn emptyList()
        }
        val order = Order(gaveUpOnDelivery, RESTAURANT_ID, 3, 1, true, mutableListOf())
        driver.receiveOrder(order)
        val log = captureLog(LogLevel.IMPORTANT)

        GlobalClock.advanceTick()
        driver.prepare()
        driver.drive()
        driver.arrive()
        driver.deliverAccepted()
        driver.deliverRejected()

        assertEquals(
            listOf("[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order ${order.getId()} to group 3."),
            logLines(log),
        )
        assertTrue(driver.isReturning())
    }

    @Test
    fun guardsIgnoreAnOrderlessDriver() {
        val log = captureLog()

        driver.loadMeals(listOf(Meal(null, casual(9, 1).members().first(), recipe(1))))
        driver.logPreparation()

        assertTrue(driver.isWaiting())
        assertTrue(driver.pendingMeals().isEmpty())
        assertTrue(logLines(log).isEmpty())
    }
}
