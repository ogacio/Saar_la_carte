package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals

/** F09: real recorded visits determine procurement, including history eviction and menu changes. */
class F09RegularHistoryPlanningTest {
    @Test
    fun onlyTheLastThreeRecordedVisitsContributeToThePackagedDeficit() {
        val rice = Ingredient("rice", UnitType.G, 100, 5)
        val oldIngredient = Ingredient("old", UnitType.G, 100, 5)
        val currentDish = dish(1, rice)
        val oldDish = dish(2, oldIngredient)
        val group = regular(1, 2)
        record(group, 1, oldDish, oldDish)
        record(group, 2, currentDish, currentDish)
        record(group, 3, currentDish, currentDish)
        record(group, 4, currentDish, currentDish)
        val pantry = Pantry(mutableListOf(rice to 20), RESTAURANT_ID)

        plan(pantry, group, currentDish, oldDish)

        // Six recorded meals need 300 g: the 280 g deficit rounds up to 300 g.
        assertEquals(320, pantry.getTotalIngredients(rice))
        assertEquals(0, pantry.getTotalIngredients(oldIngredient), "The oldest visit no longer contributes")
    }

    @Test
    fun dishesRemovedFromTheMenuDoNotAddDemandFromRecordedVisits() {
        val rice = Ingredient("rice", UnitType.G, 100, 5)
        val removedIngredient = Ingredient("removed", UnitType.G, 100, 5)
        val currentDish = dish(1, rice)
        val removedDish = dish(2, removedIngredient)
        val group = regular(1, 2)
        record(group, 1, currentDish, removedDish)
        val pantry = Pantry(mutableListOf(rice to 20), RESTAURANT_ID)

        plan(pantry, group, currentDish)

        assertEquals(120, pantry.getTotalIngredients(rice), "One remembered portion needs one additional package")
        assertEquals(0, pantry.getTotalIngredients(removedIngredient))
    }

    private fun record(group: RegularCustomerGroup, evening: Int, vararg dishes: Recipe) {
        val meals = group.members().zip(dishes.toList()) { member, dish -> Meal(null, member, dish) }
        group.recordVisit(evening, Order(group, RESTAURANT_ID, group.id(), 1, false, meals.toMutableList()))
    }

    private fun dish(id: Int, ingredient: Ingredient) = Recipe(
        id,
        "dish$id",
        10,
        setOf(CookType.EXEC),
        mutableListOf(RecipeIngredient(ingredient, 50)),
        null,
    )

    private fun plan(pantry: Pantry, group: RegularCustomerGroup, vararg recipes: Recipe) {
        val kitchen = Kitchen(
            CookRoaster(mapOf(CookType.EXEC to 1), restaurantId = RESTAURANT_ID).also { it.initialiseCooks() },
            pantry,
            mutableListOf(),
            ReservationBook(TableAssignmentService(mutableListOf())),
            RestaurantType.EUROPEAN,
        )
        val menu = Menu(recipes.toMutableList(), pantry, kitchen)
        kitchen.planEvening(mutableListOf(group), 0, menu)
    }
}
