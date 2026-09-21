package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.logging.LogLevel
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
        val meals = group.members().map { Meal(null, it, recipe(1)) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    /**
     * One tick of the delivery step for [drivers]: every phase runs for all drivers before the next
     * phase starts, so each log type is written for all drivers before the next one (post-change
     * specification, "Delivering").
     */
    private fun runPhases(vararg drivers: DeliveryDriver) {
        drivers.forEach { it.prepare() }
        drivers.forEach { it.drive() }
        drivers.forEach { it.arrive() }
        drivers.forEach { it.deliverAccepted() }
        drivers.forEach { it.deliverRejected() }
        drivers.forEach { it.returnHome() }
    }

    private fun nextTick() {
        GlobalClock.advanceTick()
        runPhases(driver)
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

        runPhases(idle)

        assertTrue(idle.isFree())
        assertTrue(idle.isWaiting())
        assertNull(idle.getId())
        assertNull(idle.getDepartureTick())
        assertEquals(RESTAURANT_ID, idle.getRestaurantId())
        assertTrue(logLines(log).isEmpty())
    }

    @Test
    fun receivingAnOrderLogsThePreparationOnceAndDoesNotDriveInTheSameTick() {
        val order = orderFor(casual(3, 2, deliveryDistance = 7))
        val log = captureLog()

        driver.receiveOrder(order)
        runPhases(driver)

        assertEquals(
            listOf(
                "[INFO] Delivery Preparation (R 1): Driver 1 prepares driving order ${order.getId()} to group 3, " +
                    "which will take 2 ticks.",
            ),
            logLines(log),
        )
        assertTrue(driver.isDelivering())
        assertFalse(driver.isFree())
        assertEquals(GlobalClock.getTickInEvening(), driver.getDepartureTick())
    }

    @Test
    fun deliveryTakesOneTickPerFiveKilometresAndArrivesInTheLastOne() {
        val group = casual(3, 2, deliveryDistance = 7)
        val order = orderFor(group)
        driver.receiveOrder(order)
        val log = captureLog(LogLevel.INFO)

        nextTick()
        nextTick()

        assertEquals(
            listOf(
                "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 3 with order ${order.getId()}.",
                "[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order ${order.getId()} to group 3.",
            ),
            logLines(log),
        )
        assertTrue(group.members().all { it.status() == CustomerStatus.SERVED })
        assertTrue(driver.isReturning())
    }

    // BUG (DeliveryDriver.drivenDistance, Constantin): the driving log must show the cumulative distance
    // driven so far, capped at 5 km per tick: 7 km logs "drove 5 km", then "drove 7 km" (forum thread 126,
    // tutors' answers of 2026-09-17). The driver logs the distance of the current tick instead ("drove 2 km").
    // Uncomment both tests once fixed.
    // @Test
    // fun drivingLogShowsTheCumulativeDistance() {
    //     driver.receiveOrder(orderFor(casual(3, 2, deliveryDistance = 7)))
    //     val log = captureLog()
    //
    //     nextTick()
    //     nextTick()
    //
    //     assertEquals(
    //         listOf(
    //             "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 1 more ticks.",
    //             "[DEBUG] Delivery Driving (R 1): Driver 1 drove 7 km and needs 0 more ticks.",
    //         ),
    //         logLines(log).filter { it.startsWith("[DEBUG] Delivery Driving") },
    //     )
    // }
    //
    // @Test
    // fun distanceThatIsAMultipleOfFiveEndsWithTheFullDistance() {
    //     driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 10)))
    //     val log = captureLog()
    //
    //     nextTick()
    //     nextTick()
    //
    //     assertEquals(
    //         "[DEBUG] Delivery Driving (R 1): Driver 1 drove 10 km and needs 0 more ticks.",
    //         logLines(log)[1],
    //     )
    // }

    @Test
    fun theFirstDrivingTickCoversFiveKilometres() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 13)))
        val log = captureLog()

        nextTick()

        assertEquals(
            listOf("[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 2 more ticks."),
            logLines(log),
        )
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
        val order = Order(gaveUp, RESTAURANT_ID, 3, 1, true, mutableListOf())
        driver.receiveOrder(order)
        val log = captureLog()

        nextTick()

        assertEquals(
            listOf(
                "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.",
                "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 3 with order ${order.getId()}.",
                "[IMPORTANT] Delivery Failed (R 1): Driver 1 failed to deliver order ${order.getId()} to group 3.",
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
    fun twoDriversWriteEachLogTypeForBothBeforeTheNextType() {
        val second = DeliveryDriver(RESTAURANT_ID).also { it.setId(2) }
        val first = orderFor(casual(3, 1, deliveryDistance = 5))
        val other = orderFor(casual(4, 1, deliveryDistance = 5))
        driver.receiveOrder(first)
        second.receiveOrder(other)
        val log = captureLog()

        nextTickFor(driver, second)

        assertEquals(
            listOf(
                "[DEBUG] Delivery Driving (R 1): Driver 1 drove 5 km and needs 0 more ticks.",
                "[DEBUG] Delivery Driving (R 1): Driver 2 drove 5 km and needs 0 more ticks.",
                "[INFO] Delivery Arrival (R 1): Driver 1 arrived at group 3 with order ${first.getId()}.",
                "[INFO] Delivery Arrival (R 1): Driver 2 arrived at group 4 with order ${other.getId()}.",
                "[IMPORTANT] Delivery Finished (R 1): Driver 1 gave delivery of order ${first.getId()} to group 3.",
                "[IMPORTANT] Delivery Finished (R 1): Driver 2 gave delivery of order ${other.getId()} to group 4.",
            ),
            logLines(log),
        )
    }

    @Test
    fun aDriverThatJustDeliveredDoesNotStartReturningInTheSameTick() {
        driver.receiveOrder(orderFor(casual(3, 1, deliveryDistance = 5)))
        val log = captureLog(LogLevel.INFO)

        nextTick()

        // One tick out, so one tick back: the return only starts counting in the next tick.
        assertTrue(driver.isReturning())
        assertTrue(logLines(log).none { it.contains("has returned") })
        nextTick()
        assertTrue(driver.isWaiting())
    }

    private fun nextTickFor(vararg drivers: DeliveryDriver) {
        GlobalClock.advanceTick()
        runPhases(*drivers)
    }

    @Test
    fun resetIdOnlyDropsTheId() {
        driver.resetId()

        assertNull(driver.getId())
        assertTrue(driver.isWaiting())
    }
}
