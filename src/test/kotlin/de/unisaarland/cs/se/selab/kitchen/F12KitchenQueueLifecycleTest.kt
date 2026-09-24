package de.unisaarland.cs.se.selab.kitchen

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * F12: what [Kitchen.cook] reports back and what survives an evening. The return value feeds the
 * "cooked" statistic, so it counts meals finished in this tick and nothing else; and "the kitchen
 * stops working ... All started or cooked meals are thrown away", so nothing queued tonight is
 * cooked tomorrow.
 */
class F12KitchenQueueLifecycleTest {

    private fun dish(id: Int, minutes: Int = 10, cookType: CookType = CookType.EXEC) =
        Recipe(id, "dish$id", minutes, setOf(cookType), mutableListOf(), null)

    private fun orderOf(recipe: Recipe, count: Int, id: Int): Order {
        val group = regular(id, count)
        val meals = MutableList(count) { Meal(null, Customer(null), recipe) }
        val order = Order(group, RESTAURANT_ID, group.id(), 1, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun kitchen(staff: Map<CookType, Int>) = Kitchen(
        CookRoaster(staff, restaurantId = RESTAURANT_ID).also { it.initialiseCooks() },
        Pantry(restaurantId = RESTAURANT_ID),
        mutableListOf(),
        ReservationBook(TableAssignmentService(mutableListOf())),
        RestaurantType.EUROPEAN,
    )

    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun anIdleKitchenCooksNothing() {
        assertEquals(0, kitchen(mapOf(CookType.EXEC to 1)).cook())
    }

    @Test
    fun cookReturnsTheMealsFinishedInThisTickOnly() {
        val k = kitchen(mapOf(CookType.EXEC to 1))
        k.enqueue(orderOf(dish(1, minutes = 30), 2, id = 1)) // three ticks

        assertEquals(0, k.cook(), "still in the pan")
        GlobalClock.advanceTick()
        assertEquals(0, k.cook())
        GlobalClock.advanceTick()
        assertEquals(2, k.cook(), "the whole batch lands in one tick")
        GlobalClock.advanceTick()
        assertEquals(0, k.cook(), "and is not counted twice")
    }

    @Test
    fun anOrderQueuedWhileTheKitchenIsBusyIsPickedUpWhenACookFreesUp() {
        val k = kitchen(mapOf(CookType.EXEC to 1))
        val first = orderOf(dish(1, minutes = 20), 1, id = 1)
        val second = orderOf(dish(2, minutes = 10), 1, id = 2)
        k.enqueue(first)
        k.cook() // the only cook takes the 20 minute dish

        k.enqueue(second)
        GlobalClock.advanceTick()
        assertEquals(1, k.cook(), "the first batch finishes, the second waits for the cook")
        assertEquals(MealStatus.QUEUED, second.getMeals().single().status)

        GlobalClock.advanceTick()
        assertEquals(1, k.cook(), "now the cook is free and the queued dish is done")
        assertEquals(MealStatus.COOKED, second.getMeals().single().status)
    }

    @Test
    fun closingTheEveningThrowsAwayEverythingStillQueued() {
        val k = kitchen(mapOf(CookType.EXEC to 1))
        val leftOver = orderOf(dish(1, minutes = 40), 2, id = 1)
        k.enqueue(leftOver)
        k.cook()
        assertTrue(leftOver.getMeals().all { it.status == MealStatus.COOKING })

        k.closeEvening()
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()

        assertEquals(0, k.cook(), "yesterday's pans are empty")
        assertTrue(k.queue.isEmpty(), "and the queue starts the evening clean")
    }

    @Test
    fun theKitchenOnlyOffersDishesItHasACookFor() {
        val k = kitchen(mapOf(CookType.EXEC to 1))

        assertTrue(k.canCook(dish(1, cookType = CookType.EXEC)))
        assertTrue(!k.canCook(dish(2, cookType = CookType.PASTRY)), "no pastry chef is employed")

        k.changeStaff(CookType.PASTRY, 1)
        assertTrue(k.canCook(dish(2, cookType = CookType.PASTRY)), "a STAFF incident unlocks the dish")
    }
}
