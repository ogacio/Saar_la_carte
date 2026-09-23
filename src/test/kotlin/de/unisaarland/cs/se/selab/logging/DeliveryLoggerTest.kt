package de.unisaarland.cs.se.selab.logging

import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class DeliveryLoggerTest {

    private fun captureAt(level: LogLevel): StringWriter {
        val buffer = StringWriter()
        LogSink.configure(level, PrintWriter(buffer))
        return buffer
    }

    @Test
    fun deliveryPreparationMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Delivery.deliveryPreparation(restaurantId = 1, driverId = 2, orderId = 3, groupId = 4, ticks = 5)
        assertEquals(
            "[INFO] Delivery Preparation (R 1): Driver 2 prepares driving order 3 to group 4, " +
                "which will take 5 ticks.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryDrivingMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Delivery.deliveryDriving(restaurantId = 1, driverId = 2, distance = 5, ticksLeft = 1)
        assertEquals(
            "[DEBUG] Delivery Driving (R 1): Driver 2 drove 5 km and needs 1 more ticks.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryArrivalMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Delivery.deliveryArrival(restaurantId = 1, driverId = 2, groupId = 4, orderId = 3)
        assertEquals(
            "[INFO] Delivery Arrival (R 1): Driver 2 arrived at group 4 with order 3.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryFinishedMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Delivery.deliveryFinished(restaurantId = 1, driverId = 2, orderId = 3, groupId = 4)
        assertEquals(
            "[IMPORTANT] Delivery Finished (R 1): Driver 2 gave delivery of order 3 to group 4.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryFailedMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Delivery.deliveryFailed(restaurantId = 1, driverId = 2, orderId = 3, groupId = 4)
        assertEquals(
            "[IMPORTANT] Delivery Failed (R 1): Driver 2 failed to deliver order 3 to group 4.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryGivenUpMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Delivery.deliveryGivenUp(restaurantId = 1, groupId = 4, orderId = 3)
        assertEquals(
            "[INFO] Delivery Given Up (R 1): Group 4 gave up on waiting for delivery of order 3.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryReturnedMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Delivery.deliveryReturned(restaurantId = 1, driverId = 2)
        assertEquals(
            "[INFO] Delivery Returned (R 1): Driver 2 has returned.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryFinishedEatingMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Delivery.deliveryFinishedEating(restaurantId = 1, groupId = 4)
        assertEquals(
            "[INFO] Delivery Finished Eating (R 1): Group 4 has finished eating.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun logsBelowTheConfiguredLevelAreSuppressed() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Delivery.deliveryDriving(restaurantId = 1, driverId = 2, distance = 5, ticksLeft = 1)
        assertEquals("", buffer.toString().trim())
    }
}
