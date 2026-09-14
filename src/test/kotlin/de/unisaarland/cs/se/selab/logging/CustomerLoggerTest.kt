package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomerLoggerTest {

    private fun captureAt(level: LogLevel): StringWriter {
        val buffer = StringWriter()
        LogSink.configure(level, PrintWriter(buffer))
        return buffer
    }

    @Test
    fun restaurantDecisionMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Customer.restaurantDecision(groupId = 3, restaurantId = 7)
        assertEquals(
            "[DEBUG] Restaurant Decision: Group 3 decided on restaurant 7.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noRestaurantDecisionMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Customer.noRestaurantDecision(groupId = 3)
        assertEquals(
            "[DEBUG] Restaurant No Decision: Group 3 could not decide for a restaurant.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun arrivalMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Customer.arrival(restaurantId = 1, groupId = 3)
        assertEquals(
            "[INFO] Restaurant Arrival (R 1): Group 3 arrived at restaurant 1.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noEatingMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Customer.noEating(restaurantId = 1, customers = 2, groupId = 3, tableId = 5)
        assertEquals(
            "[INFO] Restaurant No Eating (R 1): 2 customers of group 3 leave table 5 due to not being served.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun finishedEatingMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Customer.finishedEating(restaurantId = 1, customers = 2, groupId = 3, tableId = 5)
        assertEquals(
            "[INFO] FOH Finished Eating (R 1): 2 customers of group 3 have finished eating at table 5.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun eatingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Customer.eatingStatus(restaurantId = 1, eating = 4, eaten = 2)
        assertEquals(
            "[DEBUG] FOH Eating Status (R 1): 4 customers are eating and 2 customers have finished eating this tick.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun ratingMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Customer.rating(
            restaurantId = 1,
            groupId = 3,
            rating = Rating.POSITIVE,
            positiveRatings = 6,
            negativeRatings = 2,
        )
        assertEquals(
            "[INFO] Rating (R 1): Group 3 rates the restaurant 1 with POSITIVE rating, " +
                "leading to 6 positive ratings and 2 negative ratings.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun ratingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Customer.ratingStatus(restaurantId = 1, groups = 5)
        assertEquals(
            "[DEBUG] Rating Status (R 1): 5 groups performed ratings this tick.",
            buffer.toString().trim(),
        )
    }
}
