package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals

class SupplierTest {

    @Test
    fun enqueueAddsOrderToQueue() {
        val kitchen = kitchen()
        val order = order(id = 1, recipe = recipe(id = 1))

        kitchen.enqueue(order)

        assertEquals(listOf(order), kitchen.queue)
    }

    @Test
    fun queuedMealWithoutEligibleCookRemainsQueued() {
        startEvening()
        val kitchen = kitchen()
        val order = order(id = 1, recipe = recipe(id = 1, cookType = CookType.PASTRY))
        kitchen.enqueue(order)

        kitchen.cook()

        assertEquals(listOf(MealStatus.QUEUED), statusesOf(order))
    }

    @Test
    fun queuedMealWithEligibleFreeCookStartsCooking() {
        startEvening()
        val kitchen = kitchen()
        val order = order(id = 1, recipe = recipe(id = 1))
        kitchen.enqueue(order)

        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKING), statusesOf(order))
    }

    @Test
    fun mealsWithSameRecipeAreStartedAsOneBatch() {
        startEvening()
        val kitchen = kitchen()
        val sharedRecipe = recipe(id = 1)
        val firstOrder = order(id = 1, recipe = sharedRecipe, mealCount = 2)
        val secondOrder = order(id = 2, recipe = sharedRecipe, mealCount = 2)
        kitchen.enqueue(firstOrder)
        kitchen.enqueue(secondOrder)

        kitchen.cook()

        assertEquals(List(2) { MealStatus.COOKING }, statusesOf(firstOrder))
        assertEquals(List(2) { MealStatus.COOKING }, statusesOf(secondOrder))
    }

    @Test
    fun basicDishIsStartedBeforeNonBasicDish() {
        startEvening()
        val kitchen = kitchen()
        val nonBasic = order(id = 1, recipe = recipe(id = 1))
        val basic = order(
            id = 2,
            recipe = recipe(id = 9, basicFor = RestaurantType.EUROPEAN),
        )
        kitchen.enqueue(nonBasic)
        kitchen.enqueue(basic)

        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKING), statusesOf(basic))
        assertEquals(listOf(MealStatus.QUEUED), statusesOf(nonBasic))
    }

    @Test
    fun lowerRecipeIdIsStartedFirstWhenBasicStatusMatches() {
        startEvening()
        val kitchen = kitchen()
        val higherId = order(id = 1, recipe = recipe(id = 5))
        val lowerId = order(id = 2, recipe = recipe(id = 2))
        kitchen.enqueue(higherId)
        kitchen.enqueue(lowerId)

        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKING), statusesOf(lowerId))
        assertEquals(listOf(MealStatus.QUEUED), statusesOf(higherId))
    }

    private fun kitchen(): Kitchen {
        val roaster = CookRoaster(
            kitchenStaff = mapOf(CookType.EXEC to 1),
            restaurantId = RESTAURANT_ID,
        ).also { it.initialiseCooks() }
        return Kitchen(
            roaster = roaster,
            pantry = Pantry(restaurantId = RESTAURANT_ID),
            queue = mutableListOf(),
            reservationBook = ReservationBook(TableAssignmentService(mutableListOf())),
            restaurantType = RestaurantType.EUROPEAN,
        )
    }

    private fun recipe(
        id: Int,
        cookType: CookType = CookType.EXEC,
        basicFor: RestaurantType? = null,
    ) = Recipe(
        id = id,
        dishName = "dish$id",
        minuteDuration = 11,
        cookTypes = setOf(cookType),
        ingredients = mutableListOf(),
        basicDishFor = basicFor,
    )

    private fun order(id: Int, recipe: Recipe, mealCount: Int = 1): Order {
        val group = regular(id = id, size = mealCount)
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val order = Order(
            group = group,
            restaurantId = RESTAURANT_ID,
            customerGroupId = id,
            placedTick = GlobalClock.currentTick,
            isDelivery = false,
            meals = meals,
        )
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun statusesOf(order: Order): List<MealStatus> = order.getMeals().map { it.status }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }
}
