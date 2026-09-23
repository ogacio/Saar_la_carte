package de.unisaarland.cs.se.selab.systemtest.selab26.survivors

import de.unisaarland.cs.se.selab.systemtest.api.SystemTestAssertionError
import de.unisaarland.cs.se.selab.systemtest.selab26.utils.LogSkippingSystemTest

const val ARRIVAL = "Restaurant Arrival"
const val DECISION = "Restaurant Decision"
const val NO_DECISION = "Restaurant No Decision"
const val NO_RESERVING = "FOH No Reserving"
const val MERGING = "FOH Merging Tables"
const val SEATING = "FOH Seating ("
const val NO_SEATING = "FOH No Seating"
const val ORDERING = "FOH Ordering ("
const val NO_ORDERING = "FOH No Ordering"
const val SERVING = "FOH Serving ("
const val NO_SERVING = "FOH No Serving"
const val FOH_DELIVERY = "FOH Delivery"
const val DELIVERY = "Delivery "
const val NO_EATING = "Restaurant No Eating"
const val FINISHED_EATING = "FOH Finished Eating"
const val EATING_STATUS = "FOH Eating Status"
const val ESCORTING = "FOH Escorting ("
const val RATING = "Rating ("
const val STATISTICS = "Simulation Statistics"

/**
 * System tests aimed at the six mutants the earlier rounds never found (ShortStaffed, Backlash,
 * DinnerForOne, KingOfTheHill, FreeForAll, Arbeitszeitbetrug).
 *
 * Every test pins the complete trace of the log messages under test, each tagged "evening/tick", so
 * a mutant that moves a line to another tick, drops it or adds one fails the test, not only one
 * that changes the wording of a single line. With [group] set, only the lines about that group
 * count.
 */
abstract class SurvivorTest : LogSkippingSystemTest() {
    override val logLevel = "DEBUG"

    /** When set, the trace only keeps the lines that mention this group. */
    open val group: Int? = null

    /** Collects every message starting with one of [prefixes], tagged with its evening and tick. */
    private suspend fun trace(prefixes: List<String>): List<String> {
        val clock = Regex("Simulation: Tick (\\d+) \\((\\d+)\\)")
        val mentionsGroup = group?.let { Regex("\\bgroup $it\\b", RegexOption.IGNORE_CASE) }
        val lines = mutableListOf<String>()
        var time = "before"
        while (true) {
            val line = getNextLine() ?: break
            val tick = clock.find(line)
            if (tick != null) {
                time = "${tick.groupValues[2]}/${tick.groupValues[1]}"
                continue
            }
            val message = line.substringAfter("] ")
            val wanted = prefixes.any { message.startsWith(it) }
            if (wanted && (mentionsGroup == null || mentionsGroup.containsMatchIn(message))) {
                lines += "$time $message"
            }
        }
        return lines
    }

    /** Asserts that the trace of [prefixes] is exactly [expected], line by line. */
    protected suspend fun assertTrace(prefixes: List<String>, expected: List<String>) {
        val actual = trace(prefixes)
        if (actual == expected) return
        val first = (0 until maxOf(actual.size, expected.size)).first { actual.getOrNull(it) != expected.getOrNull(it) }
        throw SystemTestAssertionError(
            "Trace differs at line ${first + 1}: expected '${expected.getOrNull(first)}' " +
                "but got '${actual.getOrNull(first)}'. Whole trace: $actual",
        )
    }
}
