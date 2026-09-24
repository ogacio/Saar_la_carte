package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.config.restaurantParser.RestaurantParser
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class F04RestaurantParserRejectionTest {

    @TempDir
    lateinit var directory: Path

    private val model = ParsedModel()
    private val rice = Ingredient("rice", UnitType.G, 20, 5)

    private fun recipe(id: Int, dishName: String, basicFor: RestaurantType?) = Recipe(
        id = id,
        dishName = dishName,
        minuteDuration = 10,
        cookTypes = setOf(CookType.EXEC),
        ingredients = mutableListOf(RecipeIngredient(rice, 1)),
        basicDishFor = basicFor,
    )

    init {
        check(model.registerIngredient(rice))
        check(model.registerRecipe(recipe(1, "Rice Bowl", RestaurantType.ASIAN)))
        // A second recipe carrying the same dish name: valid globally, invalid inside one restaurant.
        check(model.registerRecipe(recipe(2, "Rice Bowl", null)))
        check(model.registerRecipe(recipe(3, "Noodle Bowl", null)))
    }

    private fun parse(vararg restaurants: JSONObject): Boolean {
        val input = JSONObject().put("restaurants", JSONArray(restaurants.toList()))
        val path = directory.resolve("restaurants.json")
        path.writeText(input.toString())
        return RestaurantParser(model).parse(path.toString())
    }

    private fun restaurant(
        id: Int = 1,
        name: String = "Rice House",
        type: String = "ASIAN",
        recipes: String = "[1]",
        exec: Int = 1,
        sous: Int = 0,
        tables: String = """[{"id": 1, "type": "COMMON", "size": 4}]""",
    ): JSONObject = JSONObject(
        """
        {
          "id": $id,
          "name": "$name",
          "type": "$type",
          "openingTickStart": 2,
          "openingTickEnd": 22,
          "deliveryDrivers": 0,
          "event": true,
          "positiveRatings": 0,
          "negativeRatings": 0,
          "recipes": $recipes,
          "kitchenStaff": {
            "EXEC": $exec, "SOUS": $sous, "TOURNANT": 0, "SAUCE": 0,
            "FISH": 0, "ROAST": 0, "VEGETABLE": 0, "PASTRY": 0
          },
          "waitstaff": 2,
          "tables": $tables
        }
        """.trimIndent(),
    )

    @Test
    fun twoTablesSharingAnIdAreRejected() {
        val tables = """[{"id": 1, "type": "COMMON", "size": 4}, {"id": 1, "type": "BAR", "size": 2}]"""

        assertFalse(parse(restaurant(tables = tables)), "table ids are unique per restaurant")
    }

    @Test
    fun oneRestaurantMayNotOwnTwoRecipesWithTheSameDishName() {
        assertFalse(
            parse(restaurant(recipes = "[1, 2]")),
            "\"Each restaurant may have only 1 recipe per dish name.\"",
        )
    }

    @Test
    fun twoRecipesWithDifferentDishNamesAreFine() {
        assertTrue(parse(restaurant(recipes = "[1, 3]")))

        val parsed = assertNotNull(model.restaurant(1))
        assertEquals(2, parsed.getMenu().getRecipes().size)
    }

    @Test
    fun aKitchenWithoutASingleCookIsRejected() {
        assertFalse(parse(restaurant(exec = 0)), "every cook type is zero, so nobody can cook")
    }

    @Test
    fun aTypeWithoutABasicDishInTheFoodFileIsRejected() {
        // The model only carries an ASIAN basic dish, so a EUROPEAN restaurant has none.
        assertFalse(
            parse(restaurant(type = "EUROPEAN", recipes = "[3]")),
            "every represented restaurant type needs a basic dish",
        )
    }

    @Test
    fun seatsOfTheSameTableTypeAreAddedUp() {
        val tables = """
            [{"id": 1, "type": "COMMON", "size": 4},
             {"id": 2, "type": "COMMON", "size": 6},
             {"id": 3, "type": "BAR", "size": 2}]
        """.trimIndent()

        assertTrue(parse(restaurant(tables = tables)))

        val data = assertNotNull(model.restaurant(1)).snapshot()
        assertEquals(10, data.getFreeSeats()[TableType.COMMON], "4 + 6 on the two COMMON tables")
        assertEquals(2, data.getFreeSeats()[TableType.BAR])
        assertEquals(12, data.getTotalSeats())
    }
}
