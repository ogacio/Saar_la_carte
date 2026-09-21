package de.unisaarland.cs.se.selab.kitchen

/* Kitchen.cook(): which dish is started, by whom, in which order. */

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals

class F10KitchenCookTest {

    /** A dish of [minutes] minutes; [basicFor] makes it the basic dish of that restaurant type. */
    private fun dish(
        id: Int,
        cookType: CookType = CookType.EXEC,
        basicFor: RestaurantType? = null,
        minutes: Int = 10,
    ) = Recipe(id, "dish$id", minutes, setOf(cookType), mutableListOf(), basicFor)

    /** A restaurant may have at most one EXEC cook, so more than one cook means a second type. */
    private fun kitchen(execCooks: Int = 1, sousCooks: Int = 0): Kitchen {
        val staff = mapOf(CookType.EXEC to execCooks, CookType.SOUS to sousCooks).filterValues { it > 0 }
        val roaster = CookRoaster(staff, restaurantId = RESTAURANT_ID).also { it.initialiseCooks() }
        return Kitchen(
            roaster,
            Pantry(restaurantId = RESTAURANT_ID),
            mutableListOf(),
            ReservationBook(TableAssignmentService(mutableListOf())),
            RestaurantType.EUROPEAN,
            // RESTAURANT_ID,   <- uncomment after Fix1
        )
    }

    /** An order of [count] meals of [recipe], placed in the current tick. */
    private fun order(id: Int, recipe: Recipe, count: Int): Order {
        val group = regular(id, count)
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, id, GlobalClock.currentTick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun statusesOf(order: Order) = order.getMeals().map { it.status }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** Basic dishes take precedence, even with a higher recipe id. */
    @Test
    fun basicDishesAreCookedBeforeOtherDishes() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        val basic = order(1, dish(9, basicFor = RestaurantType.EUROPEAN), 1)
        val other = order(2, dish(1), 1)
        kitchen.enqueue(other)
        kitchen.enqueue(basic)

        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKED), statusesOf(basic), "the basic dish takes the only cook")
        assertEquals(listOf(MealStatus.QUEUED), statusesOf(other), "the other dish waits")
    }

    // Commented out: asserts the recipe-id priority ACROSS two orders, but the scheduler is now
    // order-first ("Per order in the queue, the dishes of the order are assigned to a cook. Basic
    // dishes take precedence, then the lower id of the dish recipe."), so order 1 takes the only
    // cook and order 2 stays QUEUED. Rewrite with both dishes on ONE order, then re-enable.
//    /** Among dishes of one kind the lower recipe id goes first. */
//    @Test
//    fun dishesOfTheSameKindAreDecidedByRecipeId() {
//        startEvening()
//        val kitchen = kitchen(execCooks = 1)
//        val high = order(1, dish(5), 1)
//        val low = order(2, dish(2), 1)
//        kitchen.enqueue(high)
//        kitchen.enqueue(low)
//
//        kitchen.cook()
//
//        assertEquals(listOf(MealStatus.COOKED), statusesOf(low))
//        assertEquals(listOf(MealStatus.QUEUED), statusesOf(high))
//    }

    /** Meals of one dish from several orders are cooked in one batch. */
    @Test
    fun mealsOfTheSameDishFromDifferentOrdersGoIntoOneBatch() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        val soup = dish(1)
        val first = order(1, soup, 2)
        val second = order(2, soup, 2)
        kitchen.enqueue(first)
        kitchen.enqueue(second)

        kitchen.cook()

        // one cook, four meals: they only all start if they were put into a single batch
        assertEquals(List(2) { MealStatus.COOKED }, statusesOf(first))
        assertEquals(List(2) { MealStatus.COOKED }, statusesOf(second))
    }

    /** A dish nobody can cook is skipped and blocks nothing. */
    @Test
    fun aDishWithoutAnEligibleCookIsNeverAssignedAndBlocksNothing() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        // the uncookable dish has the lower id, so it would come first in the sorting
        val uncookable = order(1, dish(1, cookType = CookType.PASTRY), 1)
        val cookable = order(2, dish(2), 1)
        kitchen.enqueue(uncookable)
        kitchen.enqueue(cookable)

        kitchen.cook()

        assertEquals(listOf(MealStatus.QUEUED), statusesOf(uncookable))
        assertEquals(listOf(MealStatus.COOKED), statusesOf(cookable))
    }

    /**
     * "there is no synchronization to the kitchen, so the kitchen simply continues to try and cook
     * their meals": a group leaving the restaurant does not stop its dish.
     */
    @Test
    fun mealsOfAGroupThatLeftAreStillCooked() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        val gone = order(1, dish(1), 2)
        gone.getCustomerGroup().members().forEach { it.leave() }
        kitchen.enqueue(gone)

        kitchen.cook()

        assertEquals(List(2) { MealStatus.COOKED }, statusesOf(gone))
    }

    /** A dish without a free cook waits and starts once one is free. */
    @Test
    fun aDishWithoutAFreeCookWaitsForTheNextTick() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        val first = order(1, dish(1), 1)
        val second = order(2, dish(2, minutes = 60), 1)
        kitchen.enqueue(first)
        kitchen.enqueue(second)
        kitchen.cook()
        assertEquals(listOf(MealStatus.QUEUED), statusesOf(second), "no cook left in tick 1")

        GlobalClock.advanceTick()
        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKED), statusesOf(first), "the first dish is done")
        assertEquals(listOf(MealStatus.COOKING), statusesOf(second), "the freed cook took the second")
    }

    /** A tick with nothing to do touches nothing. */
    @Test
    fun anEmptyQueueDoesNothing() {
        startEvening()
        val kitchen = kitchen(execCooks = 0, sousCooks = 2)

        kitchen.cook()

        assertEquals(2, kitchen.roaster.cooks.count { it.isFree() }, "both cooks are still idle")
        assertEquals(listOf(null, null), kitchen.roaster.cooks.map { it.getId() }, "no id was handed out")
    }

    /** Uncooked meals do not carry over into the next evening. */
    @Test
    fun theQueueIsEmptyInTheNextEvening() {
        startEvening()
        val kitchen = kitchen(execCooks = 1)
        val leftOver = order(1, dish(1), 1)
        kitchen.enqueue(leftOver)

        kitchen.closeEvening()
        startEvening()
        kitchen.cook()

        assertEquals(listOf(MealStatus.QUEUED), statusesOf(leftOver), "yesterday's meals are not cooked today")
        assertEquals(0, kitchen.queue.size)
    }

    /** Cook ids are granted per evening and start at 1 again. */
    @Test
    fun cookIdsStartAgainAtOneEveryEvening() {
        startEvening()
        val kitchen = kitchen(execCooks = 1, sousCooks = 1)
        kitchen.enqueue(order(1, dish(1, cookType = CookType.EXEC), 1))
        kitchen.enqueue(order(2, dish(2, cookType = CookType.SOUS), 1))
        kitchen.cook()
        assertEquals(listOf(1, 2), kitchen.roaster.cooks.map { it.getId() }, "EXEC took dish 1, SOUS took dish 2")

        kitchen.closeEvening()
        startEvening()
        kitchen.enqueue(order(3, dish(3, cookType = CookType.SOUS), 1))
        kitchen.cook()

        assertEquals(listOf(null, 1), kitchen.roaster.cooks.map { it.getId() }, "the first cook of the night is 1")
    }
}
