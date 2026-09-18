package de.unisaarland.cs.se.selab.systemtest.selab26.utils

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError

/**
 * Base class for scenario system tests that only pin the log lines of the behaviour under test and
 * skip everything in between.
 */
abstract class LogSkippingSystemTest : ExampleSystemTestExtension() {

    /**
     * Reads lines until one starts with [prefix] and asserts that this line is exactly [expected].
     */
    suspend fun skipToAndAssert(prefix: String, expected: String) {
        skipUntilPrefix(prefix)
        assertCurrentLine(expected)
    }

    /**
     * Reads lines until one starts with [prefix], without looking at the rest of the line. Used by
     * flow tests, which pin the order of the steps of a tick rather than the wording of each line.
     */
    suspend fun skipToPrefix(prefix: String) = skipUntilPrefix(prefix)

    /**
     * Skips to the statistics block and asserts the four lines of restaurant [restaurantId]
     * against [cooked], [served], [delivered] and [ratings].
     */
    suspend fun assertStatistics(restaurantId: Int, cooked: Int, served: Int, delivered: Int, ratings: Int) {
        skipToAndAssert(
            "[IMPORTANT] Simulation Statistics: Restaurant $restaurantId cooked",
            "[IMPORTANT] Simulation Statistics: Restaurant $restaurantId cooked $cooked meals.",
        )
        assertNextLine("[IMPORTANT] Simulation Statistics: Restaurant $restaurantId served $served customers.")
        assertNextLine(
            "[IMPORTANT] Simulation Statistics: Restaurant $restaurantId delivered meals to $delivered customers.",
        )
        assertNextLine("[IMPORTANT] Simulation Statistics: Restaurant $restaurantId received $ratings ratings.")
    }

    private suspend fun skipUntilPrefix(prefix: String) {
        while (true) {
            val line = getNextLine() ?: throw SystemTestAssertionError("End of log reached before '$prefix'.")
            if (line.startsWith(prefix)) return
        }
    }
}
