package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.config.ConfigurationLoader
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * F01 with F02 and F31-F34: three configuration files are parsed and a whole simulation runs over the end
 * of one evening into the next, with the incidents of the first evening applied in ascending id before
 * its preparation.
 *
 * The clock is a singleton that other tests have already moved on, so the evenings and the tick limit are
 * taken relative to the clock at the start of each test. For the same reason the incidents are scheduled
 * for evening 1: every incident whose evening has been reached is applied before the next preparation.
 */
class SimulationRunIntegrationTest {

    private val dir = createTempDirectory("selab-f01").toFile()

    private fun write(name: String, content: String): String = File(dir, name).apply { writeText(content) }.path

    private fun food() = write(
        "food.json",
        """
        {"ingredients": [{"name": "rice", "unit": "g", "packagingVolume": 20, "bestBefore": 5}],
         "recipes": [{"id": 1, "dishName": "Rice Bowl", "duration": 2, "cookType": ["EXEC"],
                      "ingredients": [{"name": "rice", "amount": 50}], "basicDishFor": "ASIAN"}]}
        """.trimIndent(),
    )

    private fun restaurants() = write(
        "restaurants.json",
        """
        {"restaurants": [{"id": $RESTAURANT, "name": "Restaurant $RESTAURANT", "type": "ASIAN",
          "openingTickStart": 1, "openingTickEnd": 24, "deliveryDrivers": 0, "event": false,
          "positiveRatings": 0, "negativeRatings": 0, "recipes": [1],
          "kitchenStaff": {"EXEC": 1, "SOUS": 0, "TOURNANT": 0, "SAUCE": 0, "FISH": 0, "ROAST": 0,
                           "VEGETABLE": 0, "PASTRY": 0},
          "waitstaff": 1, "tables": [{"id": 1, "type": "COMMON", "size": 2}]}]}
        """.trimIndent(),
    )

    private fun scenario(incidents: String = "") =
        write("scenario.json", """{"customerGroups": [], "incidents": [$incidents]}""")

    private fun load(maxTicks: Int, scenario: String = scenario()): Simulator? =
        ConfigurationLoader(food(), restaurants(), scenario, maxTicks).load()

    @Test
    fun twentyFiveTicksEndTheFirstEveningAndStopInTheFirstTickOfTheSecond() {
        val firstEvening = GlobalClock.getEvening() + 1
        val simulator = assertNotNull(load(GlobalClock.currentTick + TICKS_PER_EVENING + 1))
        val log = captureLog(LogLevel.IMPORTANT)

        simulator.run()

        val second = firstEvening + 1
        val expected = listOf("[IMPORTANT] Preparation: Preparation for evening $firstEvening starts.") +
            "[IMPORTANT] Serving: Serving of evening $firstEvening starts." +
            (1..TICKS_PER_EVENING).map { "[IMPORTANT] Simulation: Tick $it ($firstEvening) started." } +
            "[IMPORTANT] Serving: Serving of evening $firstEvening ends." +
            "[IMPORTANT] Preparation: Preparation for evening $second starts." +
            "[IMPORTANT] Serving: Serving of evening $second starts." +
            "[IMPORTANT] Simulation: Tick 1 ($second) started." +
            statistics()
        assertEquals(expected, logLines(log))
    }

    @Test
    fun aFullEveningEndsWithTheServingPhaseAndNeverPreparesTheNextOne() {
        val evening = GlobalClock.getEvening() + 1
        val simulator = assertNotNull(load(GlobalClock.currentTick + TICKS_PER_EVENING))
        val log = captureLog(LogLevel.IMPORTANT)

        simulator.run()

        val lines = logLines(log)
        assertEquals("[IMPORTANT] Simulation: Tick $TICKS_PER_EVENING ($evening) started.", lines[lines.size - 7])
        assertEquals("[IMPORTANT] Serving: Serving of evening $evening ends.", lines[lines.size - 6])
        assertEquals(statistics(), lines.takeLast(5))
    }

    @Test
    fun incidentsAreLoggedInAscendingIdBeforeThePreparationAndApplied() {
        val evening = GlobalClock.getEvening() + 1
        val incidents = """
            {"id": 2, "type": "STAFF", "evening": 1, "restaurant": $RESTAURANT, "number": 1, "staffType": "WAITSTAFF"},
            {"id": 1, "type": "RECIPE", "evening": 1, "ingredient": "rice", "adaptation": 10}
        """.trimIndent()
        val simulator = assertNotNull(load(GlobalClock.currentTick + 1, scenario(incidents)))
        val log = captureLog(LogLevel.IMPORTANT)

        simulator.run()

        assertEquals(
            listOf(
                "[IMPORTANT] Incident: Incident 1 of type RECIPE occurred before evening $evening.",
                "[IMPORTANT] Incident: Incident 2 of type STAFF occurred before evening $evening.",
                "[IMPORTANT] Preparation: Preparation for evening $evening starts.",
            ),
            logLines(log).take(3),
        )
        // The RECIPE incident reaches the recipe before the first tick has refreshed any browsing data.
        val recipe = assertNotNull(simulator.restaurantsById(RESTAURANT)).getMenu().getRecipes().single()
        assertEquals(ADAPTED_RICE, recipe.ingredients.single().amount)
    }

    @Test
    fun anInvalidFileStopsTheLoadingBeforeTheLaterFiles() {
        val log = captureLog(LogLevel.INFO)
        val broken = write("broken.json", """{"customerGroups": []}""")

        assertNull(load(1, broken))

        assertEquals(
            listOf(
                "[INFO] Initialization Info: food.json successfully parsed and validated.",
                "[INFO] Initialization Info: restaurants.json successfully parsed and validated.",
                "[IMPORTANT] Initialization Info: broken.json is invalid.",
            ),
            logLines(log),
        )
    }

    private fun statistics() = listOf(
        "[IMPORTANT] Simulation Info: Simulation statistics are calculated.",
        "[IMPORTANT] Simulation Statistics: Restaurant $RESTAURANT cooked 0 meals.",
        "[IMPORTANT] Simulation Statistics: Restaurant $RESTAURANT served 0 customers.",
        "[IMPORTANT] Simulation Statistics: Restaurant $RESTAURANT delivered meals to 0 customers.",
        "[IMPORTANT] Simulation Statistics: Restaurant $RESTAURANT received 0 ratings.",
    )

    /** Values shared by the tests; the restaurant id is used by no other test. */
    private companion object {
        const val RESTAURANT = 981
        const val TICKS_PER_EVENING = 24

        /** 50 g of rice after +10 %. */
        const val ADAPTED_RICE = 55
    }
}
