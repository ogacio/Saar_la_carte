package de.unisaarland.cs.se.selab

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * F01: the CLI entry point. Every argument is required, so the parser itself is exercised by the
 * system tests; what a unit test can add is the validation of the two values the parser cannot
 * check (maxTicks and logLevel) and the wiring of a real run into an output file.
 */
class F01MainEntryPointTest {

    private val fixtures = "src/systemtest/resources/foh/p05_backlash"

    private fun args(
        maxTicks: String = "2",
        logLevel: String = "INFO",
        out: String? = null,
        food: String = "$fixtures/food_rice.json",
        restaurants: String = "$fixtures/restaurants_one_small_table.json",
        scenario: String = "$fixtures/scenario_oversized_regular.json",
    ): Array<String> = buildList {
        addAll(listOf("--food", food, "--restaurants", restaurants, "--scenario", scenario))
        addAll(listOf("--maxTicks", maxTicks, "--logLevel", logLevel))
        if (out != null) addAll(listOf("--out", out))
    }.toTypedArray()

    private fun tempOut(name: String): File =
        File.createTempFile(name, ".log").also { it.deleteOnExit() }

    @Test
    fun aCompleteRunWritesTheSimulationLogToTheRequestedFile() {
        val out = tempOut("f01-run")

        main(args(out = out.path))

        val log = out.readLines().filter { it.isNotBlank() }
        assertTrue(log.isNotEmpty(), "the run produced no output at all")
        assertContains(log.first(), "food_rice.json successfully parsed and validated.")
        assertTrue(log.any { it.contains("Simulation Info: Simulation started.") }, log.first())
    }

    @Test
    fun anInvalidConfigurationStopsBeforeTheSimulationStarts() {
        val out = tempOut("f01-invalid")

        main(args(scenario = "$fixtures/food_rice.json", out = out.path))

        val log = out.readLines().filter { it.isNotBlank() }
        assertTrue(log.any { it.contains("is invalid.") }, "the bad scenario should be rejected: $log")
        assertTrue(
            log.none { it.contains("Simulation started") },
            "no simulator means no run: $log",
        )
    }

    @Test
    fun withoutAnOutputFileTheLogGoesToStandardOutput() {
        // run() wraps System.out in a PrintWriter and closes it through `use`, so the real stdout is
        // swapped for a buffer first and restored afterwards; closing the real one would silence
        // every later test in this JVM.
        val captured = ByteArrayOutputStream()
        val original = System.out
        try {
            System.setOut(PrintStream(captured, true))
            main(args(logLevel = "IMPORTANT"))
        } finally {
            System.setOut(original)
        }

        val log = captured.toString().lines().filter { it.isNotBlank() }
        assertTrue(log.any { it.contains("Preparation for evening 1 starts.") }, "stdout got: $log")
    }

    @Test
    fun maxTicksMustBeInRange() {
        assertFailsWith<IllegalArgumentException> { main(args(maxTicks = "-1")) }
        assertFailsWith<IllegalArgumentException> { main(args(maxTicks = "1001")) }
    }

    @Test
    fun theLogLevelIsMatchedAgainstTheEnumNamesCaseSensitively() {
        val lower = assertFailsWith<IllegalArgumentException> { main(args(logLevel = "info")) }
        assertContains(lower.message.orEmpty(), "--logLevel must be DEBUG, INFO or IMPORTANT")

        assertFailsWith<IllegalArgumentException> { main(args(logLevel = "VERBOSE")) }
    }
}
