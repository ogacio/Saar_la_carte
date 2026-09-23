package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FoodParserTest {

    @TempDir
    lateinit var directory: Path

    private fun write(name: String, content: String): String {
        val path = directory.resolve(name)
        path.writeText(content)
        return path.toString()
    }

    private fun foodWith(
        ingredients: String = """{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}""",
        recipes: String = """
            {
              "id": 1,
              "dishName": "Rice Bowl",
              "duration": 10,
              "cookType": ["EXEC"],
              "ingredients": [{"name": "rice", "amount": 50}],
              "basicDishFor": "ASIAN"
            }
        """.trimIndent(),
    ) = """
        {
          "ingredients": [$ingredients],
          "recipes": [$recipes]
        }
    """.trimIndent()

    @Test
    fun validFoodFileRegistersIngredientsAndRecipes() {
        val model = ParsedModel()

        assertTrue(FoodParser(model).parse(write("food.json", foodWith())))

        assertNotNull(model.ingredient("rice"))
        assertNotNull(model.recipe(1))
    }

    @Test
    fun unknownUnitRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            ingredients = """{"name": "rice", "unit": "kg", "packagingVolume": 100, "bestBefore": 5}""",
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
        assertNull(model.ingredient("rice"))
    }

    @Test
    fun nonPositivePackagingVolumeRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            ingredients = """{"name": "rice", "unit": "g", "packagingVolume": 0, "bestBefore": 5}""",
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun nonPositiveBestBeforeRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            ingredients = """{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 0}""",
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun negativeRecipeIdRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": -1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun durationBelowTheMinimumRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 1,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun durationAboveTheMaximumRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 41,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun unknownCookTypeRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["NOTACOOK"],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun emptyCookTypeListRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": [],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun emptyIngredientsListRejectsTheRecipe() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": []
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun recipeIngredientReferencingAnUnknownIngredientRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "unobtainium", "amount": 50}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun nonPositiveRecipeIngredientAmountRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 0}]
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun unknownBasicDishForRejectsTheFile() {
        val model = ParsedModel()
        val food = foodWith(
            recipes = """
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}],
                  "basicDishFor": "MEXICAN"
                }
            """.trimIndent(),
        )

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun duplicateBasicDishNameAcrossRecipesRejectsTheFile() {
        val model = ParsedModel()
        val food = """
            {
              "ingredients": [{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}],
              "recipes": [
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}],
                  "basicDishFor": "ASIAN"
                },
                {
                  "id": 2,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}],
                  "basicDishFor": "EUROPEAN"
                }
              ]
            }
        """.trimIndent()

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun aSecondRecipeOfTheSameDishNameWithoutBasicDishForIsAllowed() {
        val model = ParsedModel()
        val food = """
            {
              "ingredients": [{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}],
              "recipes": [
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}],
                  "basicDishFor": "ASIAN"
                },
                {
                  "id": 2,
                  "dishName": "Rice Bowl",
                  "duration": 15,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 70}]
                }
              ]
            }
        """.trimIndent()

        assertTrue(FoodParser(model).parse(write("food.json", food)))
        assertNotNull(model.recipe(1))
        assertNotNull(model.recipe(2))
    }

    @Test
    fun emptyIngredientsArrayInTheWholeFileRejectsTheFile() {
        val model = ParsedModel()
        val food = """
            {
              "ingredients": [],
              "recipes": [
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}]
                }
              ]
            }
        """.trimIndent()

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun emptyRecipesArrayRejectsTheFile() {
        val model = ParsedModel()
        val food = """
            {
              "ingredients": [{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}],
              "recipes": []
            }
        """.trimIndent()

        assertFalse(FoodParser(model).parse(write("food.json", food)))
    }

    @Test
    fun malformedJsonRejectsTheFile() {
        val model = ParsedModel()

        assertFalse(FoodParser(model).parse(write("food.json", "{ this is not valid json")))
    }

    @Test
    fun missingFileRejectsParsing() {
        val model = ParsedModel()

        assertFalse(FoodParser(model).parse(directory.resolve("does-not-exist.json").toString()))
    }

    @Test
    fun twoRecipesWithDifferentDishNamesEachRegisterTheirOwnBasicDish() {
        val model = ParsedModel()
        val food = """
            {
              "ingredients": [{"name": "rice", "unit": "g", "packagingVolume": 100, "bestBefore": 5}],
              "recipes": [
                {
                  "id": 1,
                  "dishName": "Rice Bowl",
                  "duration": 10,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 50}],
                  "basicDishFor": "ASIAN"
                },
                {
                  "id": 2,
                  "dishName": "Fried Rice",
                  "duration": 12,
                  "cookType": ["EXEC"],
                  "ingredients": [{"name": "rice", "amount": 60}],
                  "basicDishFor": "EUROPEAN"
                }
              ]
            }
        """.trimIndent()

        assertTrue(FoodParser(model).parse(write("food.json", food)))
        assertEquals(setOf("Rice Bowl"), model.basicDishesFor(RestaurantType.ASIAN))
    }
}
