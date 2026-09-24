package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import kotlin.test.Test
import kotlin.test.assertEquals

/** F09: aggregate demand before subtracting existing stock and rounding the deficit. */
class F09PlanningStockBoundaryTest {
    @Test
    fun anExactPackageDeficitDoesNotBuyAnExtraPackage() {
        assertStockAfterPlanning(initial = 20, requirement = 120, expected = 120)
    }

    @Test
    fun oneGramBeyondAnExactPackageDeficitRequiresAnotherPackage() {
        assertStockAfterPlanning(initial = 20, requirement = 121, expected = 220)
    }

    @Test
    fun sharedIngredientDemandIsCombinedBeforeSubtractingStockAndRounding() {
        val rice = Ingredient("rice", UnitType.G, 100, 5)
        val pantry = Pantry(mutableListOf(rice to 20), RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(dish(1, rice, 60), dish(2, rice, 60)), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), 10, menu)

        // 60 + 60 - 20 = 100: one package, not one package per recipe.
        assertEquals(120, pantry.getTotalIngredients(rice))
    }

    @Test
    fun eachIngredientUsesItsOwnDeficitAndPackageSize() {
        val rice = Ingredient("rice", UnitType.G, 100, 5)
        val beans = Ingredient("beans", UnitType.G, 30, 5)
        val salt = Ingredient("salt", UnitType.G, 10, 5)
        val pantry = Pantry(mutableListOf(rice to 20, beans to 25, salt to 70), RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val recipe = dish(1, rice, 121).also { it.ingredients.add(RecipeIngredient(beans, 56)) }
        val menu = Menu(mutableListOf(recipe), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), 10, menu)

        assertEquals(220, pantry.getTotalIngredients(rice))
        assertEquals(85, pantry.getTotalIngredients(beans))
        assertEquals(70, pantry.getTotalIngredients(salt), "Unrelated stock must remain available")
    }

    private fun assertStockAfterPlanning(initial: Int, requirement: Int, expected: Int) {
        val rice = Ingredient("rice", UnitType.G, 100, 5)
        val pantry = Pantry(mutableListOf(rice to initial), RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(dish(1, rice, requirement)), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), 10, menu)

        assertEquals(expected, pantry.getTotalIngredients(rice))
    }

    private fun dish(id: Int, ingredient: Ingredient, amount: Int) = Recipe(
        id,
        "dish$id",
        10,
        setOf(CookType.EXEC),
        mutableListOf(RecipeIngredient(ingredient, amount)),
        null,
    )

    private fun kitchen(pantry: Pantry) = Kitchen(
        CookRoaster(mapOf(CookType.EXEC to 1), restaurantId = RESTAURANT_ID).also { it.initialiseCooks() },
        pantry,
        mutableListOf(),
        ReservationBook(TableAssignmentService(mutableListOf())),
        RestaurantType.EUROPEAN,
    )
}
