package de.unisaarland.cs.se.selab.simulation

/* F07: the cooked meals of the statistics, which no other test touches. */

import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F07CookedStatisticTest {

    /**
     * Statistics is a global object that cannot be reset, so every test uses a restaurant id of its
     * own and reads the four lines of that restaurant out of the report.
     */
    private fun linesOf(restaurantId: Int): List<String> {
        val log = captureLog(LogLevel.IMPORTANT)
        Statistics.report(listOf(restaurantId))
        return logLines(log).filter { it.contains("Restaurant $restaurantId ") }
    }

    /** Cooked meals are counted and reported. */
    @Test
    fun cookedMealsAreCountedPerRestaurant() {
        Statistics.recordCooked(9071, 2)
        Statistics.recordCooked(9071, 3)

        assertTrue(
            linesOf(9071).any { it.endsWith("cooked 5 meals.") },
            "the cooked meals were not counted: ${linesOf(9071)}",
        )
    }

    /** A restaurant that cooked nothing reports zero rather than nothing. */
    @Test
    fun aRestaurantThatCookedNothingReportsZero() {
        assertTrue(linesOf(9072).any { it.endsWith("cooked 0 meals.") })
    }

    /** Cooked, served and delivered are three different counters. */
    @Test
    fun cookedIsSeparateFromServedAndDelivered() {
        Statistics.recordCooked(9073, 7)
        Statistics.record(9073, 2, delivered = false)
        Statistics.record(9073, 4, delivered = true)

        val lines = linesOf(9073)
        assertTrue(lines.any { it.endsWith("cooked 7 meals.") }, "cooked: $lines")
        assertTrue(lines.any { it.endsWith("served 2 customers.") }, "served: $lines")
        assertTrue(lines.any { it.endsWith("delivered meals to 4 customers.") }, "delivered: $lines")
    }

    /** The meals of one restaurant do not land in the count of another. */
    @Test
    fun theCookedMealsOfOneRestaurantDoNotLeakIntoAnother() {
        Statistics.recordCooked(9074, 3)

        assertTrue(linesOf(9075).any { it.endsWith("cooked 0 meals.") })
    }

    /** The four lines of a restaurant always come in the same order. */
    @Test
    fun theFourLinesOfARestaurantComeInAFixedOrder() {
        Statistics.recordCooked(9076, 1)

        val lines = linesOf(9076)
        assertEquals(4, lines.size, "four lines per restaurant: $lines")
        assertTrue(lines[0].contains("cooked"), lines[0])
        assertTrue(lines[1].contains("served"), lines[1])
        assertTrue(lines[2].contains("delivered"), lines[2])
        assertTrue(lines[3].contains("ratings"), lines[3])
    }
}
