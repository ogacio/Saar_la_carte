package de.unisaarland.cs.se.selab

/*
 * F01 "Simulation - managing Main, CLI".
 */

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class F01MainCliTest {

    private fun args(maxTicks: String, logLevel: String) = arrayOf(
        "--food", "food.json",
        "--restaurants", "restaurants.json",
        "--scenario", "scenario.json",
        "--maxTicks", maxTicks,
        "--logLevel", logLevel,
    )

    /** "maxTicks (maxTicks in [0; 1000])" - one above the limit is refused. */
    @Test
    fun moreThanAThousandTicksIsRefused() {
        val error = assertFailsWith<IllegalArgumentException> { main(args("1001", "DEBUG")) }

        assertTrue(error.message.orEmpty().contains("maxTicks"), "the message names the argument: ${error.message}")
    }

    /** A negative number of ticks is refused as well. */
    @Test
    fun aNegativeNumberOfTicksIsRefused() {
        assertFailsWith<IllegalArgumentException> { main(args("-1", "DEBUG")) }
    }

    /** The three log levels of the specification are DEBUG, INFO and IMPORTANT - nothing else. */
    @Test
    fun anUnknownLogLevelIsRefused() {
        val error = assertFailsWith<IllegalArgumentException> { main(args("10", "VERBOSE")) }

        assertTrue(error.message.orEmpty().contains("logLevel"), "the message names the argument: ${error.message}")
    }

    /** The level is matched against the enum names, so the lower case spelling is not a level. */
    @Test
    fun theLogLevelIsCaseSensitive() {
        assertFailsWith<IllegalArgumentException> { main(args("10", "debug")) }
    }
}
