package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.config.FoodParser
import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.config.restaurantParser.RestaurantParser
import de.unisaarland.cs.se.selab.config.scenarioParser.ScenarioParser
import de.unisaarland.cs.se.selab.incident.IncidentType
import de.unisaarland.cs.se.selab.incident.PackagingChange
import de.unisaarland.cs.se.selab.incident.StaffChange
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConfigParsingIntegrationTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun validRestaurantCustomerAndIncidentAreParsedTogether() {
        val model = ParsedModel()

        assertTrue(FoodParser(model).parse(write("food.json", food())))
        assertTrue(RestaurantParser(model).parse(write("restaurants.json", restaurant())))
        assertTrue(ScenarioParser(model).parse(write("scenario.json", validScenario())))

        assertNotNull(model.restaurant(RESTAURANT_ID))
        assertNotNull(model.customerGroup(CUSTOMER_ID))
        val staff = assertIs<StaffChange>(model.incident(STAFF_INCIDENT_ID))
        val packaging = assertIs<PackagingChange>(model.incident(PACKAGING_INCIDENT_ID))
        assertEquals(IncidentType.STAFF, staff.type)
        assertEquals(IncidentType.PACKAGING, packaging.type)
    }

    @Test
    fun restaurantReferenceToUnknownRecipeRejectsConfiguration() {
        val model = ParsedModel()
        assertTrue(FoodParser(model).parse(write("food.json", food())))

        val parsed = RestaurantParser(model).parse(
            write("restaurants.json", restaurant(recipeId = 999)),
        )

        assertFalse(parsed)
        assertNull(model.restaurant(RESTAURANT_ID))
    }

    @Test
    fun scenarioReferencesMustExistInPreviouslyParsedConfiguration() {
        val customerModel = parsedFoodAndRestaurant()
        val unknownRestaurant = ScenarioParser(customerModel).parse(
            write("unknown-restaurant.json", scenarioWithRegular(restaurantId = 999)),
        )
        assertFalse(unknownRestaurant)

        val incidentModel = parsedFoodAndRestaurant()
        val unknownIngredient = ScenarioParser(incidentModel).parse(
            write("unknown-ingredient.json", scenarioWithPackaging(ingredient = "unobtainium")),
        )
        assertFalse(unknownIngredient)
    }

    private fun parsedFoodAndRestaurant(): ParsedModel {
        val model = ParsedModel()
        assertTrue(FoodParser(model).parse(write("food.json", food())))
        assertTrue(RestaurantParser(model).parse(write("restaurants.json", restaurant())))
        return model
    }

    private fun write(name: String, content: String): String {
        val path = directory.resolve(name)
        path.writeText(content)
        return path.toString()
    }

    private fun food(): String =
        """
        {
          "ingredients": [
            {"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}
          ],
          "recipes": [
            {
              "id": 1,
              "dishName": "Rice Bowl",
              "duration": 10,
              "cookType": ["EXEC"],
              "ingredients": [{"name": "rice", "amount": 50}],
              "basicDishFor": "ASIAN"
            }
          ]
        }
        """.trimIndent()

    private fun restaurant(recipeId: Int = 1): String =
        """
        {
          "restaurants": [
            {
              "id": $RESTAURANT_ID,
              "name": "Rice House",
              "type": "ASIAN",
              "openingTickStart": 1,
              "openingTickEnd": 24,
              "deliveryDrivers": 0,
              "event": false,
              "positiveRatings": 0,
              "negativeRatings": 0,
              "recipes": [$recipeId],
              "kitchenStaff": {
                "EXEC": 1,
                "SOUS": 0,
                "TOURNANT": 0,
                "SAUCE": 0,
                "FISH": 0,
                "ROAST": 0,
                "VEGETABLE": 0,
                "PASTRY": 0
              },
              "waitstaff": 1,
              "tables": [{"id": 1, "type": "COMMON", "size": 4}]
            }
          ]
        }
        """.trimIndent()

    private fun validScenario(): String =
        """
        {
          "customerGroups": [
            {
              "id": $CUSTOMER_ID,
              "type": "REGULAR",
              "size": 2,
              "tableType": "COMMON",
              "visitingTick": 5,
              "foodPreferences": [],
              "visitingStart": 1,
              "visitingPeriod": 2,
              "restaurant": $RESTAURANT_ID
            }
          ],
          "incidents": [
            {
              "id": $STAFF_INCIDENT_ID,
              "type": "STAFF",
              "evening": 2,
              "restaurant": $RESTAURANT_ID,
              "number": 1,
              "staffType": "WAITSTAFF"
            },
            {
              "id": $PACKAGING_INCIDENT_ID,
              "type": "PACKAGING",
              "evening": 3,
              "ingredient": "rice",
              "packagingVolume": 250
            }
          ]
        }
        """.trimIndent()

    private fun scenarioWithRegular(restaurantId: Int): String =
        """
        {
          "customerGroups": [
            {
              "id": $CUSTOMER_ID,
              "type": "REGULAR",
              "size": 2,
              "visitingTick": 5,
              "foodPreferences": [],
              "visitingStart": 1,
              "visitingPeriod": 2,
              "restaurant": $restaurantId
            }
          ],
          "incidents": []
        }
        """.trimIndent()

    private fun scenarioWithPackaging(ingredient: String): String =
        """
        {
          "customerGroups": [],
          "incidents": [
            {
              "id": $PACKAGING_INCIDENT_ID,
              "type": "PACKAGING",
              "evening": 3,
              "ingredient": "$ingredient",
              "packagingVolume": 250
            }
          ]
        }
        """.trimIndent()

    private companion object {
        const val RESTAURANT_ID = 7
        const val CUSTOMER_ID = 11
        const val STAFF_INCIDENT_ID = 21
        const val PACKAGING_INCIDENT_ID = 22
    }
}
