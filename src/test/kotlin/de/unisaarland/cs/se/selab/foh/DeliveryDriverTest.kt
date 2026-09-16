package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
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

/** F20: one delivery driver driving out, delivering and returning. */
class DeliveryDriverTest {

    private lateinit var driver: DeliveryDriver

    private fun orderFor(group: CustomerGroup): Order {
        val order = Order(5, group, RESTAURANT_ID, group.id(), 1, true, mutableListOf())
        group.members().forEach { order.meals.add(Meal(order.id, it, recipe(1))) }
        return order
    }

    private fun nextTick() {
        GlobalClock.advanceTick()
        driver.plusTick()
    }

    @BeforeTest
    fun freshEveningAndDriver() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        driver = DeliveryDriver(RESTAURANT_ID).also { it.setId(1) }
    }

    @Test
    fun newDriverWaitsAndIgnoresTicks() {
        val idle = DeliveryDriver(RESTAURANT_ID)
        val log = captureLog()

        idle.plusTick()

        assertTrue(idle.isFree())
        assertTrue(idle.isWaiting())
        assertNull(idle.getId())
        assertNull(idle.getDepartureTick())
        assertEquals(RESTAURANT_ID, idle.getRestaurantId())
        assertTrue(logLines(log).isEmpty())
    }

    @Test
    fun receivingAnOrderLogsThePreparationAndDoesNotDriveInTheSameTick() {
        val log = captureLog()

        driver.receiveOrder(orderFor(casual(3, 2, deliveryDistance = 7)))
        driver.plusTick()

        assertEquals(
            listOf(
                "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order 5 to group 3, " +
                    "which will take 2 ticks.",
            ),
            logLines(log),
        )
        assertTrue(driver.isDelivering())
        assertFalse(driver.isFree())
        assertEquals(GlobalClock.getTickInEvening(), driver.getDepartureTick())
    }

    @Test
    fun deliveryDrivesFiveKilometresPerTickAndTheRestInTheLastTick() {
        val group = casual(3, 2, deliveryDistance = 7)
        driver.receiveOrder(orderFor(group))
        val log = captureLog()

        nextTick()
        nextTick()

        assertEquals(
            listOf(
                "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.",
                "[DEBUG] Delivery Driving (R 1): Driver 1 drove 2 km and needs 0 more ticks.",
                "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 3 with order 5.",
                "[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order 5 to group 3.",
            ),
            logLines(log),
        )
        assertTrue(group.members().all { it.status() == CustomerStatus.SERVED })
        assertTrue(driver.isReturning())
    }

    @Test
    fun distanceThatIsAMultipleOfFiveDrivesFiveKilometresInTheLastTick() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 10)))
        val log = captureLog()

        nextTick()
        nextTick()

        assertEquals("[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.", logLines(log)[1])
    }

    @Test
    fun returnTakesAsLongAsTheDriveAndKeepsTheIdForTheEvening() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 7)))
        nextTick()
        nextTick()
        val log = captureLog()

        nextTick()
        assertTrue(logLines(log).isEmpty())
        nextTick()

        assertEquals(listOf("[INFO] Delivery Returned (R 1): Driver 1 has returned."), logLines(log))
        assertTrue(driver.isWaiting())
        assertEquals(1, driver.getId())
        assertNull(driver.getDepartureTick())
    }

    @Test
    fun groupThatGaveUpDoesNotReceiveTheDelivery() {
        val gaveUp = mock<CustomerGroup> {
            on { id() } doReturn 3
            on { hasGivenUp() } doReturn true
            on { getDeliveryDistance() } doReturn 5
            on { members() } doReturn emptyList()
        }
        driver.receiveOrder(Order(5, gaveUp, RESTAURANT_ID, 3, 1, true, mutableListOf()))
        val log = captureLog()

        nextTick()

        assertEquals(
            listOf(
                "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.",
                "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 3 with order 5.",
                "[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order 5 to group 3.",
            ),
            logLines(log),
        )
        assertTrue(driver.isReturning())
    }

    @Test
    fun driverSwitchedDuringTheReturnLosesItsIdOnArrival() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))
        nextTick()
        driver.switch()
        captureLog()

        nextTick()

        assertTrue(driver.isWaiting())
        assertNull(driver.getId())
    }

    @Test
    fun abortDropsTheDeliveryAndTheId() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))

        driver.abort()

        assertTrue(driver.isWaiting())
        assertNull(driver.getId())
        assertNull(driver.getDepartureTick())
    }

    @Test
    fun stateQueriesAnswerForExactlyOneState() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))
        captureLog()

        assertFalse(driver.isWaiting())
        assertFalse(driver.isReturning())
        nextTick()
        assertFalse(driver.isDelivering())
        assertTrue(driver.isReturning())
    }

    @Test
    fun switchingTwiceKeepsTheIdOnReturn() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))
        nextTick()
        driver.switch()
        driver.switch()
        captureLog()

        nextTick()

        assertEquals(1, driver.getId())
    }

    @Test
    fun resetIdOnlyDropsTheId() {
        driver.resetId()

        assertNull(driver.getId())
        assertTrue(driver.isWaiting())
    }
}
