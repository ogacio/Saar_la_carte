package de.unisaarland.cs.se.selab.config

/*
 * F01 "Simulation - managing Main, CLI, and simulation, integrating parsing".
 */

import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F01ConfigurationLoaderTest {

    /** A file with [content] that disappears when the JVM ends. */
    private fun fileWith(name: String, content: String): File {
        val file = File.createTempFile(name, ".json")
        file.deleteOnExit()
        file.writeText(content)
        return file
    }

    /** The smallest food file the parser accepts: one ingredient, one basic recipe. */
    private fun validFood() = fileWith(
        "food",
        """
        {
          "ingredients": [
            { "name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5 }
          ],
          "recipes": [
            {
              "id": 1,
              "dishName": "Rice Bowl",
              "duration": 10,
              "cookType": ["EXEC"],
              "ingredients": [{ "name": "rice", "amount": 50 }],
              "basicDishFor": "EUROPEAN"
            }
          ]
        }
        """.trimIndent(),
    )

    /**
     * "the simulation stops as soon as one of the files is invalid": an unreadable food file ends
     * the loading, and the two later files are never even opened - the paths below do not exist.
     */
    @Test
    fun anInvalidFoodFileEndsTheLoadingBeforeTheOtherFilesAreRead() {
        val food = fileWith("broken", "this is not json")
        val log = captureLog()

        val simulator = ConfigurationLoader(
            food.path,
            "does/not/exist/restaurants.json",
            "does/not/exist/scenario.json",
            maxTicks = 10,
        ).load()

        assertNull(simulator, "an invalid food file cannot produce a simulation")
        assertEquals(
            listOf("[IMPORTANT] Initialization Info: ${food.name} is invalid."),
            logLines(log),
            "only the food file may be reported, and it must be reported as invalid",
        )
    }

    /**
     * The order of the three stages is food, then restaurants, then scenario. With a valid food
     * file and a broken restaurants file, the food file is reported as parsed, the restaurants file
     * as invalid, and the scenario file is never touched.
     */
    @Test
    fun theFilesAreReadInTheOrderFoodRestaurantsScenario() {
        val food = validFood()
        val restaurants = fileWith("restaurants", "{ \"restaurants\": [] }")
        val log = captureLog()

        val simulator = ConfigurationLoader(
            food.path,
            restaurants.path,
            "does/not/exist/scenario.json",
            maxTicks = 10,
        ).load()

        assertNull(simulator)
        val lines = logLines(log)
        assertEquals(2, lines.size, "one line per file that was read: $lines")
        assertTrue(
            lines[0].endsWith("${food.name} successfully parsed and validated."),
            "food first: ${lines[0]}",
        )
        assertTrue(lines[1].endsWith("${restaurants.name} is invalid."), "restaurants second: ${lines[1]}")
    }

    /**
     * The log names the file, not the path: a scenario in a deep directory is reported by its plain
     * file name, because that is what the expected output of the tests contains.
     */
    @Test
    fun theLogNamesTheFileWithoutItsDirectory() {
        val food = fileWith("broken", "{")
        val log = captureLog()

        ConfigurationLoader(food.path, "r.json", "s.json", maxTicks = 1).load()

        val line = logLines(log).single()
        assertTrue(line.contains(food.name), "the file name belongs in the line: $line")
        assertTrue(
            !line.contains(File.separator) && !line.contains("/"),
            "the directory does not belong in the line: $line",
        )
    }
}
