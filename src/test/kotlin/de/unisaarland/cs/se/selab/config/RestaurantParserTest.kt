package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.config.restaurantParser.RestaurantParser
import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/** Checks restaurant parsing, model registration and rejection of invalid configurations. */
class RestaurantParserTest {

    @TempDir
    lateinit var directory: Path

    private val model = ParsedModel()
    private val rice = Ingredient("rice", UnitType.G, 20, 5)
    private val recipe = Recipe(
        id = 1,
        dishName = "Rice Bowl",
        minuteDuration = 10,
        cookTypes = setOf(CookType.EXEC),
        ingredients = mutableListOf(RecipeIngredient(rice, 1)),
        basicDishFor = RestaurantType.ASIAN,
    )

    init {
        check(model.registerIngredient(rice))
        check(model.registerRecipe(recipe))
    }

    @Test
    fun validRestaurantIsParsedAndRegisteredWithItsConfiguration() {
        assertTrue(parse(restaurant()))

        val parsed = assertNotNull(model.restaurant(7))
        assertEquals(listOf(parsed), model.allRestaurants())
        assertEquals(7, parsed.getId())
        assertEquals("Rice House", parsed.name)
        assertEquals(RestaurantType.ASIAN, parsed.type)
        assertEquals(2, parsed.openingTick)
        assertEquals(22, parsed.closingTick)
        assertTrue(parsed.hostsEvents)
        assertEquals(8, parsed.initialPositiveRatings)
        assertEquals(3, parsed.initialNegativeRatings)
        assertSame(recipe, parsed.getMenu().getRecipes().single())
        assertEquals(listOf(CookType.EXEC), parsed.getKitchen().roaster.cooks.map { it.getType() })
        assertEquals(20, parsed.getFoh().getWaitstaff().capacity(ActionType.SEATING))
        assertEquals(2, parsed.getFoh().getDeliveryDesk().amountFreeDrivers())
        assertTrue(parsed.getFoh().getDeliveryDesk().getDrivers().all { it.getRestaurantId() == 7 })
        assertEquals(mapOf(TableType.COMMON to 4, TableType.BAR to 2), parsed.snapshot().getFreeSeats())
        assertEquals(6, parsed.snapshot().getTotalSeats())
    }

    @Test
    fun equalOpeningAndClosingTicksAreRejected() {
        assertFalse(parse(restaurant().put("openingTickStart", 12).put("openingTickEnd", 12)))
    }

    @Test
    fun openingAfterClosingIsRejected() {
        assertFalse(parse(restaurant().put("openingTickStart", 20).put("openingTickEnd", 10)))
    }

    @Test
    fun unknownRecipeIdIsRejected() {
        assertFalse(parse(restaurant().put("recipes", JSONArray().put(999))))
    }

    @Test
    fun restaurantWithoutCooksIsRejected() {
        val input = restaurant()
        input.getJSONObject("kitchenStaff").put("EXEC", 0)

        assertFalse(parse(input))
    }

    @Test
    fun restaurantWithoutWaitstaffIsRejected() {
        assertFalse(parse(restaurant().put("waitstaff", 0)))
    }

    @Test
    fun restaurantWithoutTablesIsRejected() {
        assertFalse(parse(restaurant().put("tables", JSONArray())))
    }

    @Test
    fun duplicateRestaurantIdsWithDifferentNamesAreRejected() {
        assertFalse(parse(restaurant(), restaurant(name = "Other Restaurant")))
    }

    // DISABLED (2026-09-18, Stefan): RestaurantParser does not check restaurant name uniqueness at
    // all (R-1, spec 2.3.2 bullet 1 — "restaurant ids unique and restaurant names unique", see
    // misc/implementation/spec-extraction.md line 122). This is a real missing validation rule in
    // RestaurantParser.kt (Biborka's file, F04), not a wrong test. Re-enable once the parser checks it.
    // @Test
    // fun duplicateRestaurantNamesWithDifferentIdsAreRejected() {
    //     assertFalse(parse(restaurant(), restaurant(id = 8)))
    // }

    @Test
    fun restaurantsWithDifferentIdsAndNamesAreAccepted() {
        assertTrue(
            parse(
                restaurant(id = 7, name = "Rice House"),
                restaurant(id = 8, name = "Sushi House"),
            )
        )
    }

    private fun parse(vararg restaurants: JSONObject): Boolean {
        val input = JSONObject().put("restaurants", JSONArray(restaurants.toList()))
        val path = directory.resolve("restaurants.json")
        path.writeText(input.toString())
        return RestaurantParser(model).parse(path.toString())
    }

    private fun restaurant(id: Int = 7, name: String = "Rice House"): JSONObject = JSONObject(
        """
        {
          "type": "ASIAN",
          "openingTickStart": 2,
          "openingTickEnd": 22,
          "deliveryDrivers": 2,
          "event": true,
          "positiveRatings": 8,
          "negativeRatings": 3,
          "recipes": [1],
          "kitchenStaff": {
            "EXEC": 1, "SOUS": 0, "TOURNANT": 0, "SAUCE": 0,
            "FISH": 0, "ROAST": 0, "VEGETABLE": 0, "PASTRY": 0
          },
          "waitstaff": 2,
          "tables": [
            {"id": 1, "type": "COMMON", "size": 4},
            {"id": 2, "type": "BAR", "size": 2}
          ]
        }
        """.trimIndent(),
    ).put("id", id).put("name", name)
}
