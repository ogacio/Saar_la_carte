package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * F07: statistics. [Statistics] is a singleton without a reset, so every test uses restaurant ids
 * of its own.
 */
class StatisticsTest {

    @Test
    fun restaurantWithoutRecordsReportsZeros() {
        val log = captureLog(LogLevel.IMPORTANT)

        Statistics.report(listOf(901))

        assertEquals(
            listOf(
                "[IMPORTANT] Simulation Info: Simulation statistics are calculated.",
                "[IMPORTANT] Simulation Statistics: Restaurant 901 cooked 0 meals.",
                "[IMPORTANT] Simulation Statistics: Restaurant 901 served 0 customers.",
                "[IMPORTANT] Simulation Statistics: Restaurant 901 delivered meals to 0 customers.",
                "[IMPORTANT] Simulation Statistics: Restaurant 901 received 0 ratings.",
            ),
            logLines(log),
        )
    }

    @Test
    fun recordsAccumulateAndServedIsSeparateFromDelivered() {
        Statistics.recordCooked(902, 3)
        Statistics.recordCooked(902, 4)
        Statistics.record(902, 2, delivered = false)
        Statistics.record(902, 5, delivered = false)
        Statistics.record(902, 4, delivered = true)
        Statistics.record(902, 2, delivered = true)
        Statistics.recordRating(902)
        Statistics.recordRating(902)
        val log = captureLog(LogLevel.IMPORTANT)

        Statistics.report(listOf(902))

        assertEquals(
            listOf(
                "[IMPORTANT] Simulation Info: Simulation statistics are calculated.",
                "[IMPORTANT] Simulation Statistics: Restaurant 902 cooked 7 meals.",
                "[IMPORTANT] Simulation Statistics: Restaurant 902 served 7 customers.",
                "[IMPORTANT] Simulation Statistics: Restaurant 902 delivered meals to 6 customers.",
                "[IMPORTANT] Simulation Statistics: Restaurant 902 received 2 ratings.",
            ),
            logLines(log),
        )
    }

    @Test
    fun recordsOfOneRestaurantDoNotLeakIntoAnother() {
        Statistics.recordCooked(903, 9)
        val log = captureLog(LogLevel.IMPORTANT)

        Statistics.report(listOf(904))

        assertEquals("[IMPORTANT] Simulation Statistics: Restaurant 904 cooked 0 meals.", logLines(log)[1])
    }

    @Test
    fun restaurantsAreReportedInAscendingId() {
        val log = captureLog(LogLevel.IMPORTANT)

        Statistics.report(listOf(906, 905))

        val restaurantLines = logLines(log).drop(1).map { it.substringAfter("Restaurant ").substringBefore(" ") }
        assertEquals(listOf("905", "905", "905", "905", "906", "906", "906", "906"), restaurantLines)
    }

    @Test
    fun reportWithoutRestaurantsOnlyLogsTheHeader() {
        val log = captureLog(LogLevel.DEBUG)

        Statistics.report(emptyList())

        assertEquals(listOf("[IMPORTANT] Simulation Info: Simulation statistics are calculated."), logLines(log))
    }
}
