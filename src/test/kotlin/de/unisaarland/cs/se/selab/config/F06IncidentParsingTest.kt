package de.unisaarland.cs.se.selab.config

/* F06: the constraints on incidents that the JSON schema cannot express. */

import de.unisaarland.cs.se.selab.config.scenarioParser.IncidentJsonDto
import de.unisaarland.cs.se.selab.config.scenarioParser.IncidentSerialiser
import de.unisaarland.cs.se.selab.incident.IncidentType
import de.unisaarland.cs.se.selab.incident.StaffChange
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.simulation.Restaurant
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class F06IncidentParsingTest {

    private val rice = Ingredient("rice", UnitType.G, 100, 5)
    private lateinit var parser: IncidentSerialiser

    /** A model that knows one ingredient and one restaurant, so references can be resolved. */
    @BeforeTest
    fun modelWithOneRestaurantAndOneIngredient() {
        val model = ParsedModel()
        model.registerIngredient(rice)
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getId()).thenReturn(1)
        model.registerRestaurant(restaurant)
        parser = IncidentSerialiser(model)
    }

    /** A staff change entry of the scenario file. */
    private fun staff(
        restaurant: Int? = null,
        number: Int? = null,
        staffType: String? = null,
        cookType: String? = null,
        type: String = "STAFF",
    ) = IncidentJsonDto(1, type, 2, restaurant, number, staffType, cookType, null, null, null, null)

    /** An entry of the scenario file that changes something about an ingredient. */
    private fun ingredientEntry(
        type: String,
        ingredient: String? = "rice",
        evening: Int = 2,
        adaptation: Int? = null,
        packagingVolume: Int? = null,
        duration: Int? = null,
    ) = IncidentJsonDto(1, type, evening, null, null, null, null, ingredient, adaptation, packagingVolume, duration)

    /** A staff change with every field set becomes an incident of that type. */
    @Test
    fun aCompleteStaffChangeIsAccepted() {
        val incident = parser.serialise(staff(restaurant = 1, number = 2, staffType = "COOK", cookType = "SOUS"))

        assertNotNull(incident)
        assertEquals(IncidentType.STAFF, incident.type)
        assertEquals(2, incident.evening)
    }

    /** A staff change for a restaurant that does not exist cannot be applied. */
    @Test
    fun aStaffChangeForAnUnknownRestaurantIsRejected() {
        assertNull(parser.serialise(staff(restaurant = 99, number = 1, staffType = "COOK")))
    }

    /** Without the restaurant there is nothing to change. */
    @Test
    fun aStaffChangeWithoutARestaurantIsRejected() {
        assertNull(parser.serialise(staff(number = 1, staffType = "WAITSTAFF")))
    }

    /** The number of staff members is what the incident is about, so it may not be missing. */
    @Test
    fun aStaffChangeWithoutANumberIsRejected() {
        assertNull(parser.serialise(staff(restaurant = 1, staffType = "WAITSTAFF")))
    }

    /** Only COOK, WAITSTAFF and DRIVER are staff types. */
    @Test
    fun aStaffChangeWithAnUnknownStaffTypeIsRejected() {
        assertNull(parser.serialise(staff(restaurant = 1, number = 1, staffType = "MANAGER")))
    }

    /** A cook type that is not one of the eight positions is not a cook type. */
    @Test
    fun aStaffChangeWithAnUnknownCookTypeIsRejected() {
        assertNull(parser.serialise(staff(restaurant = 1, number = 1, staffType = "COOK", cookType = "GRILL")))
    }

    /** Waitstaff and drivers have no cook type, so leaving it out is fine. */
    @Test
    fun aStaffChangeWithoutACookTypeIsAccepted() {
        assertNotNull(parser.serialise(staff(restaurant = 1, number = 1, staffType = "DRIVER")))
    }

    /** Firing staff is a negative number, which is a valid incident too. */
    @Test
    fun aStaffChangeMayRemoveStaff() {
        val incident = parser.serialise(staff(restaurant = 1, number = -1, staffType = "COOK", cookType = "PASTRY"))

        assertIs<StaffChange>(assertNotNull(incident))
    }

    /** A type outside the four of the specification is not an incident. */
    @Test
    fun anUnknownIncidentTypeIsRejected() {
        assertNull(parser.serialise(staff(restaurant = 1, number = 1, staffType = "COOK", type = "EARTHQUAKE")))
    }

    /** A recipe change adapts the amount of an ingredient the food file knows. */
    @Test
    fun aCompleteRecipeChangeIsAccepted() {
        val incident = parser.serialise(ingredientEntry("RECIPE", adaptation = 20))

        assertEquals(IncidentType.RECIPE, assertNotNull(incident).type)
    }

    /** An ingredient that is not in the food file cannot be adapted. */
    @Test
    fun aRecipeChangeForAnUnknownIngredientIsRejected() {
        assertNull(parser.serialise(ingredientEntry("RECIPE", ingredient = "saffron", adaptation = 20)))
    }

    /** Without the percentage there is no adaptation. */
    @Test
    fun aRecipeChangeWithoutAnAdaptationIsRejected() {
        assertNull(parser.serialise(ingredientEntry("RECIPE")))
    }

    /** A packaging change needs the new volume. */
    @Test
    fun aPackagingChangeWithoutAVolumeIsRejected() {
        assertNull(parser.serialise(ingredientEntry("PACKAGING")))
    }

    /** An unavailability lasts a number of evenings, which has to be given. */
    @Test
    fun anUnavailabilityWithoutADurationIsRejected() {
        assertNull(parser.serialise(ingredientEntry("UNAVAILABLE")))
    }

    /** A complete unavailability is accepted and keeps its evening. */
    @Test
    fun aCompleteUnavailabilityIsAccepted() {
        val incident = parser.serialise(ingredientEntry("UNAVAILABLE", evening = 3, duration = 2))

        assertEquals(IncidentType.UNAVAILABLE, assertNotNull(incident).type)
        assertEquals(3, incident.evening)
    }
}
