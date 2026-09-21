package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.config.scenarioParser.IncidentJsonDto
import de.unisaarland.cs.se.selab.config.scenarioParser.IncidentSerialiser
import de.unisaarland.cs.se.selab.config.scenarioParser.ScenarioParser
import de.unisaarland.cs.se.selab.incident.IncidentType
import de.unisaarland.cs.se.selab.incident.IngredientUnavailability
import de.unisaarland.cs.se.selab.incident.PackagingChange
import de.unisaarland.cs.se.selab.incident.StaffChange
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.StaffType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.Simulator
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Checks conversion and file-level validation of all four incident types. */
class IncidentSerialiserTest {

    @TempDir
    lateinit var directory: Path

    private val model = ParsedModel()
    private val rice = Ingredient("rice", UnitType.G, 100, 3)
    private val serialiser = IncidentSerialiser(model)

    init {
        check(model.registerIngredient(rice))
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getId()).thenReturn(RESTAURANT_ID)
        check(model.registerRestaurant(restaurant))
    }

    @Test
    fun validStaffIncidentHasCorrectTypeAndValues() {
        val incident = assertIs<StaffChange>(
            serialiser.serialise(
                incident(
                    id = 1,
                    type = "STAFF",
                    evening = 4,
                ).copy(
                    restaurant = RESTAURANT_ID,
                    number = -2,
                    staffType = "COOK",
                    cookType = "SOUS",
                ),
            ),
        )
        val restaurant = requireNotNull(model.restaurant(RESTAURANT_ID))
        val simulator = mock<Simulator>()
        whenever(simulator.restaurantsById(RESTAURANT_ID)).thenReturn(restaurant)

        incident.apply(simulator)

        assertEquals(1, incident.id)
        assertEquals(4, incident.evening)
        assertEquals(IncidentType.STAFF, incident.type)
        verify(restaurant).changeStaff(StaffType.COOK, CookType.SOUS, -2)
    }

    @Test
    fun staffFieldsAreValid() {
        assertNotNull(
            serialiser.serialise(
                incident(id = 20, type = "STAFF", evening = 2).copy(
                    restaurant = RESTAURANT_ID,
                    number = 1,
                    staffType = "COOK",
                    cookType = "SOUS",
                ),
            ),
        )

        assertNull(
            serialiser.serialise(
                incident(id = 21, type = "STAFF", evening = 2).copy(
                    restaurant = RESTAURANT_ID,
                    number = 1,
                    staffType = "COOK",
                    cookType = "EXEC",
                ),
            ),
        )

        assertNull(
            serialiser.serialise(
                incident(id = 22, type = "STAFF", evening = 2).copy(
                    restaurant = RESTAURANT_ID,
                    number = 1,
                    staffType = "COOK",
                    cookType = null,
                ),
            ),
        )
    }

    @Test
    fun validPackagingIncidentHasCorrectTypeAndValues() {
        val incident = assertIs<PackagingChange>(
            serialiser.serialise(
                incident(id = 3, type = "PACKAGING", evening = 6).copy(
                    ingredient = "rice",
                    packagingVolume = 250,
                ),
            ),
        )

        incident.apply(mock())

        assertEquals(3, incident.id)
        assertEquals(6, incident.evening)
        assertEquals(IncidentType.PACKAGING, incident.type)
        assertEquals(250, rice.packagingVolume)
    }

    @Test
    fun validUnavailableIncidentHasCorrectTypeAndValues() {
        val incident = assertIs<IngredientUnavailability>(
            serialiser.serialise(
                incident(id = 4, type = "UNAVAILABLE", evening = 7).copy(ingredient = "rice", duration = 3),
            ),
        )

        assertEquals(4, incident.id)
        assertEquals(7, incident.evening)
        assertEquals(IncidentType.UNAVAILABLE, incident.type)
    }

    @Test
    fun staffIncidentWithUnknownRestaurantIsRejected() {
        val input = incident(
            id = 5,
            type = "STAFF",
            evening = 2,
        ).copy(
            restaurant = 999,
            number = 1,
            staffType = "WAITSTAFF",
        )

        assertNull(serialiser.serialise(input))
    }

    @Test
    fun staffIncidentCannotChangeExecCooks() {
        val input = incident(
            id = 6,
            type = "STAFF",
            evening = 2,
        ).copy(
            restaurant = RESTAURANT_ID,
            number = 1,
            staffType = "COOK",
            cookType = "EXEC",
        )

        assertNull(serialiser.serialise(input))
    }

    @Test
    fun recipeIncidentWithUnknownIngredientIsRejected() {
        val input = incident(id = 7, type = "RECIPE", evening = 2).copy(
            ingredient = "unknown",
            adaptation = 10,
        )

        assertNull(serialiser.serialise(input))
    }

    @Test
    fun packagingIncidentWithUnknownIngredientIsRejected() {
        val input = incident(id = 8, type = "PACKAGING", evening = 2).copy(
            ingredient = "unknown",
            packagingVolume = 50,
        )

        assertNull(serialiser.serialise(input))
    }

    @Test
    fun unavailableIncidentWithUnknownIngredientIsRejected() {
        val input = incident(id = 9, type = "UNAVAILABLE", evening = 2).copy(
            ingredient = "unknown",
            duration = 2,
        )

        assertNull(serialiser.serialise(input))
    }

    @Test
    fun overlappingUnavailableWindowsForSameIngredientAreRejected() {
        val first = unavailableJson(id = 10, evening = 2, duration = 3)
        val second = unavailableJson(id = 11, evening = 4, duration = 2)

        assertFalse(parseScenario(first, second))
    }

    @Test
    fun directlyAdjacentUnavailableWindowsAreAccepted() {
        val first = unavailableJson(id = 12, evening = 2, duration = 2)
        val second = unavailableJson(id = 13, evening = 4, duration = 2)

        assertTrue(parseScenario(first, second))
        assertIs<IngredientUnavailability>(model.incident(12))
        assertIs<IngredientUnavailability>(model.incident(13))
    }

    private fun parseScenario(vararg incidents: JSONObject): Boolean {
        val root = JSONObject()
            .put("customerGroups", JSONArray())
            .put("incidents", JSONArray(incidents.toList()))
        val path = directory.resolve("scenario.json")
        path.writeText(root.toString())
        return ScenarioParser(model).parse(path.toString())
    }

    private fun unavailableJson(id: Int, evening: Int, duration: Int): JSONObject = JSONObject()
        .put("id", id)
        .put("type", "UNAVAILABLE")
        .put("evening", evening)
        .put("ingredient", "rice")
        .put("duration", duration)

    private fun incident(
        id: Int,
        type: String,
        evening: Int,
    ) = IncidentJsonDto(
        id = id,
        type = type,
        evening = evening,
    )

    private companion object {
        const val RESTAURANT_ID = 7
    }
}
