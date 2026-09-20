package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.members
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals

class PantryTest {

    @Test
    fun completeRequiredAmountIsNotBoughtAgain() {
        val rice = ingredient(packagingVolume = 100)
        val pantry = Pantry(mutableListOf(rice to 250), restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = menu(kitchen, pantry, recipe(id = 1, ingredient = rice, amount = 250))

        kitchen.planEvening(mutableListOf(), otherSeats = 10, menu)

        assertEquals(250, pantry.getTotalIngredients(rice))
    }

    @Test
    fun onlyRoundedMissingAmountIsBoughtForPartialStock() {
        val rice = ingredient(packagingVolume = 100)
        val pantry = Pantry(mutableListOf(rice to 175), restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = menu(kitchen, pantry, recipe(id = 1, ingredient = rice, amount = 250))

        kitchen.planEvening(mutableListOf(), otherSeats = 10, menu)

        assertEquals(275, pantry.getTotalIngredients(rice))
    }

    @Test
    fun regularExpectedDishesIncreaseIngredientDemand() {
        val rice = ingredient(packagingVolume = 10)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val dish = recipe(id = 1, ingredient = rice, amount = 40)
        val menu = menu(kitchen, pantry, dish)
        val group = regular(id = 1, size = 2)
        group.recordVisit(evening = 1, o = order(group, dish))

        kitchen.planEvening(mutableListOf(group), otherSeats = 0, menu)

        assertEquals(80, pantry.getTotalIngredients(rice))
    }

    @Test
    fun eventExpectedDishesIncreaseIngredientDemand() {
        repeat(4) { GlobalClock.advanceEvening() }
        val evening = GlobalClock.getEvening()
        val rice = ingredient(packagingVolume = 10)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        val reservations = ReservationBook(TableAssignmentService(mutableListOf()))
        val kitchen = kitchen(pantry, reservations)
        val dish = recipe(id = 1, ingredient = rice, amount = 40)
        val menu = menu(kitchen, pantry, dish)
        reservations.bookAhead(eventGroup(id = 1, size = 3, evening = evening), evening)

        kitchen.planEvening(mutableListOf(), otherSeats = 0, menu)

        assertEquals(120, pantry.getTotalIngredients(rice))
    }

    @Test
    fun casualSeatEstimationRoundsUpPerTenSeatsForEveryRecipe() {
        listOf(10 to 50, 11 to 100).forEach { (seats, expectedAmount) ->
            val rice = ingredient(name = "rice", packagingVolume = 10)
            val beans = ingredient(name = "beans", packagingVolume = 10)
            val pantry = Pantry(restaurantId = RESTAURANT_ID)
            val kitchen = kitchen(pantry)
            val menu = menu(
                kitchen,
                pantry,
                recipe(id = 1, ingredient = rice, amount = 50),
                recipe(id = 2, ingredient = beans, amount = 50),
            )

            kitchen.planEvening(mutableListOf(), otherSeats = seats, menu)

            assertEquals(expectedAmount, pantry.getTotalIngredients(rice), "rice for $seats seats")
            assertEquals(expectedAmount, pantry.getTotalIngredients(beans), "beans for $seats seats")
        }
    }

    private fun ingredient(name: String = "rice", packagingVolume: Int) =
        Ingredient(name, UnitType.G, packagingVolume, bestUntil = 5)

    private fun recipe(id: Int, ingredient: Ingredient, amount: Int) = Recipe(
        id = id,
        dishName = "dish$id",
        minuteDuration = 10,
        cookTypes = setOf(CookType.EXEC),
        ingredients = mutableListOf(RecipeIngredient(ingredient, amount)),
        basicDishFor = null,
    )

    private fun kitchen(pantry: Pantry, reservations: ReservationBook = emptyReservations()): Kitchen = Kitchen(
        roaster = CookRoaster(
            kitchenStaff = mapOf(CookType.EXEC to 1),
            restaurantId = RESTAURANT_ID,
        ).also { it.initialiseCooks() },
        pantry = pantry,
        queue = mutableListOf(),
        reservationBook = reservations,
        restaurantType = RestaurantType.EUROPEAN,
    )

    private fun menu(kitchen: Kitchen, pantry: Pantry, vararg recipes: Recipe) =
        Menu(recipes.toMutableList(), pantry, kitchen)

    private fun order(group: RegularCustomerGroup, recipe: Recipe): Order {
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        return Order(group, RESTAURANT_ID, group.id(), 1, false, meals)
    }

    private fun eventGroup(id: Int, size: Int, evening: Int) = EventCustomerGroup(
        id = id,
        groupSize = size,
        tableType = TableType.COMMON,
        visitingTick = 1,
        members = members(size),
        preferences = emptyList(),
        restaurantTypes = setOf(RestaurantType.EUROPEAN),
        eventEvening = evening,
        favouriteDishes = mapOf(RestaurantType.EUROPEAN to "dish1"),
    )

    private fun emptyReservations() = ReservationBook(TableAssignmentService(mutableListOf()))
}
