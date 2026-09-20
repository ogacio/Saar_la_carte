package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import kotlin.test.Test
import kotlin.test.assertEquals

class PantrySupplierTest {

    @Test
    fun planningBuysOnlyMissingAmountRoundedToPackagingVolume() {
        val fixture = planningFixture(initialStock = 175)

        fixture.kitchen.planEvening(mutableListOf(), otherSeats = 10, fixture.menu)

        assertEquals(275, fixture.pantry.getTotalIngredients(fixture.rice))
    }

    @Test
    fun planningWithEnoughStockDoesNotBuyAdditionalIngredients() {
        val fixture = planningFixture(initialStock = 250)

        fixture.kitchen.planEvening(mutableListOf(), otherSeats = 10, fixture.menu)

        assertEquals(250, fixture.pantry.getTotalIngredients(fixture.rice))
    }

    private fun planningFixture(initialStock: Int): PlanningFixture {
        val rice = Ingredient("rice", UnitType.G, packagingVolume = 100, bestUntil = 5)
        val pantry = Pantry(
            stock = mutableListOf(rice to initialStock),
            restaurantId = RESTAURANT_ID,
        )
        val reservations = ReservationBook(TableAssignmentService(mutableListOf()))
        val roaster = CookRoaster(
            kitchenStaff = mapOf(CookType.EXEC to 1),
            restaurantId = RESTAURANT_ID,
        ).also { it.initialiseCooks() }
        val kitchen = Kitchen(
            roaster = roaster,
            pantry = pantry,
            queue = mutableListOf(),
            reservationBook = reservations,
            restaurantType = RestaurantType.EUROPEAN,
        )
        val recipe = Recipe(
            id = 1,
            dishName = "Rice Bowl",
            minuteDuration = 10,
            cookTypes = setOf(CookType.EXEC),
            ingredients = mutableListOf(RecipeIngredient(rice, amount = 250)),
            basicDishFor = RestaurantType.EUROPEAN,
        )
        val menu = Menu(mutableListOf(recipe), pantry, kitchen)
        return PlanningFixture(rice, pantry, kitchen, menu)
    }

    private data class PlanningFixture(
        val rice: Ingredient,
        val pantry: Pantry,
        val kitchen: Kitchen,
        val menu: Menu,
    )

    private companion object {
        const val RESTAURANT_ID = 7
    }
}
