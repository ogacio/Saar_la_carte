package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.ActionType
import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F19: serving - the hold-back window, capacity waiting, EVENT priority and delivery hand-off. */
class ServingServiceTest {

    private fun basicRecipe(id: Int) =
        Recipe(id, "basic$id", 10, setOf(CookType.EXEC), mutableListOf(), RestaurantType.EUROPEAN)

    private fun cookedMeal(customer: de.unisaarland.cs.se.selab.sharedPackage.customers.Customer, recipe: Recipe) =
        Meal(null, customer, recipe, status = MealStatus.COOKED)

    private fun visitWith(group: CustomerGroup, waiters: List<Waiter>, meals: MutableList<Meal>): Visit {
        waiters.forEachIndexed { index, waiter -> if (waiter.id == null) waiter.id = index + 1 }
        val visit = Visit(group)
        val table = Table(1, group.members().size, group.tableType())
        val tick = GlobalClock.getTickInEvening()
        visit.seated(table, waiters, tick)
        val order = Order(group, RESTAURANT_ID, group.id(), tick, false, meals)
        visit.ordered(order, tick)
        return visit
    }

    private fun withRegisteredDriver(desk: DeliveryDesk, block: () -> Unit) {
        val foh = mock<FrontOfTheHouse>()
        whenever(foh.getDeliveryDesk()).thenReturn(desk)
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getFoh()).thenReturn(foh)
        val sim = mock<Simulator>()
        whenever(sim.restaurantsById(RESTAURANT_ID)).thenReturn(restaurant)
        DeliveryService.setSimulator(sim)
        DeliveryService.addDriver(RESTAURANT_ID)
        try {
            block()
        } finally {
            DeliveryService.rmDriver(RESTAURANT_ID)
        }
    }

    private fun serviceWith(vararg waiters: Waiter) = ServingService(
        WaiterAssignmentService(waiters.toMutableList()),
        DeliveryDesk(mutableListOf(), RESTAURANT_ID),
        RestaurantType.EUROPEAN,
    )

    @BeforeTest
    fun freshEveningAndTick() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    @Test
    fun holdBackWindowServesNothingOnTheTickTheFirstMealCooksOrTheNextOne() {
        val recipe = basicRecipe(1)
        val group = casual(3, 2)
        val cooked = cookedMeal(group.members()[0], recipe)
        val stillCooking = Meal(null, group.members()[1], recipe, status = MealStatus.COOKING)
        val meals = mutableListOf(cooked, stillCooking)
        val waiter = Waiter()
        val visit = visitWith(group, listOf(waiter), meals)
        val service = serviceWith(waiter)
        val log = captureLog()

        service.serve(listOf(visit), subUnits())

        assertTrue(logLines(log).any { it.contains("FOH No Serving") && it.contains("Waitstaff 1") })
        assertEquals(MealStatus.COOKED, cooked.status)
    }

    @Test
    fun windowExpiresAndMealsGoOutOneByOne() {
        val recipe = basicRecipe(1)
        val group = casual(3, 1)
        val meals = mutableListOf(cookedMeal(group.members()[0], recipe))
        val waiter = Waiter()
        val visit = visitWith(group, listOf(waiter), meals)
        val service = serviceWith(waiter)

        service.serve(listOf(visit), subUnits())
        GlobalClock.advanceTick()
        waiter.beginTick()
        GlobalClock.advanceTick()
        waiter.beginTick()
        service.serve(listOf(visit), subUnits())

        assertEquals(MealStatus.SERVED, meals[0].status)
    }

    @Test
    fun allMealsCookedIsServedImmediatelyWithoutHoldBack() {
        val recipe = basicRecipe(1)
        val group = casual(3, 2)
        val meals = group.members().map { cookedMeal(it, recipe) }.toMutableList()
        val waiter = Waiter()
        val visit = visitWith(group, listOf(waiter), meals)
        val service = serviceWith(waiter)

        service.serve(listOf(visit), subUnits())

        assertTrue(meals.all { it.status == MealStatus.SERVED })
    }

    @Test
    fun basicDishesAreServedBeforeNonBasicWhenCapacityIsShort() {
        val basic = basicRecipe(1)
        val nonBasic = Recipe(2, "special", 10, setOf(CookType.EXEC), mutableListOf(), null)
        val group = casual(3, 2)
        val nonBasicMeal = cookedMeal(group.members()[0], nonBasic)
        val basicMeal = cookedMeal(group.members()[1], basic)
        val meals = mutableListOf(nonBasicMeal, basicMeal)
        val waiter = Waiter()
        val visit = visitWith(group, listOf(waiter), meals)
        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val service = serviceWith(waiter)

        service.serve(listOf(visit), subUnits())
        GlobalClock.advanceTick()
        waiter.beginTick()
        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        service.serve(listOf(visit), subUnits())

        assertEquals(MealStatus.SERVED, basicMeal.status)
        assertEquals(MealStatus.COOKED, nonBasicMeal.status)
    }

    @Test
    fun capacityShortAndInHoldBackWindowServesNothingThisTick() {
        val recipe = basicRecipe(1)
        val group = casual(3, 3)
        val meals = group.members().map { cookedMeal(it, recipe) }.toMutableList()
        val waiter = Waiter()
        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val visit = visitWith(group, listOf(waiter), meals)
        val service = serviceWith(waiter)

        service.serve(listOf(visit), subUnits())

        assertTrue(meals.all { it.status == MealStatus.COOKED })
    }

    @Test
    fun staffWorkedExampleTickOneNamesTheLeastCapacityWaiterEvenThoughOthersHaveMoreRoom() {
        val recipe = basicRecipe(1)
        val group = event(3, 30)
        val meals = group.members().map { cookedMeal(it, recipe) }.toMutableList()
        val w1 = Waiter().also { it.id = 1 }
        val w2 = Waiter().also { it.id = 2 }
        val w3 = Waiter().also { it.id = 3 }
        w1.consume(ActionType.SERVING, 5)
        val visit = visitWith(group, emptyList(), meals)
        val service = serviceWith(w1, w2, w3)
        val log = captureLog()

        service.serve(listOf(visit), subUnits())

        assertTrue(
            logLines(log).any {
                it.contains("FOH No Serving") && it.contains("Waitstaff 1") && it.contains("30")
            },
        )
        assertTrue(meals.all { it.status == MealStatus.COOKED })
    }

    @Test
    fun staffWorkedExampleTickTwoServesOneByOneAndNamesTheLastWaiterToServe() {
        val recipe = basicRecipe(1)
        val group = event(3, 30)
        val meals = group.members().map { cookedMeal(it, recipe) }.toMutableList()
        val w1 = Waiter().also { it.id = 1 }
        val w2 = Waiter().also { it.id = 2 }
        val w3 = Waiter().also { it.id = 3 }
        w1.consume(ActionType.SERVING, 5)
        val visit = visitWith(group, emptyList(), meals)
        val service = serviceWith(w1, w2, w3)
        service.serve(listOf(visit), subUnits())

        GlobalClock.advanceTick()
        w1.beginTick()
        w2.beginTick()
        w3.beginTick()
        w1.consume(ActionType.SERVING, 5)
        val log = captureLog()
        service.serve(listOf(visit), subUnits())

        assertEquals(25, meals.count { it.status == MealStatus.SERVED })
        assertTrue(
            logLines(log).any { it.contains("FOH No Serving") && it.contains("Waitstaff 3") && it.contains(" 5") },
        )
    }

    @Test
    fun deliveryHandOffIsRefusedWhenCapacityCannotCoverTheWholeOrder() {
        val recipe = basicRecipe(1)
        val group = casual(9, 1, deliveryDistance = 3)
        val order = Order(
            group,
            RESTAURANT_ID,
            group.id(),
            1,
            true,
            mutableListOf(cookedMeal(group.members()[0], recipe)),
        )
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        desk.enqueue(order)
        desk.readyOrder(order)
        val waiter = Waiter()
        waiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT)
        val service = ServingService(WaiterAssignmentService(mutableListOf(waiter)), desk, RestaurantType.EUROPEAN)

        service.serve(emptyList(), subUnits())

        assertTrue(desk.getReady().contains(order))
    }

    @Test
    fun deliveryHandOffSpansMultipleWaitersWhenOneLacksCapacity() {
        val recipe = basicRecipe(1)
        val group = casual(9, 3, deliveryDistance = 3)
        val meals = group.members().map { cookedMeal(it, recipe) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), 1, true, meals)
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        desk.enqueue(order)
        desk.readyOrder(order)
        val first = Waiter()
        val second = Waiter()
        first.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val service = ServingService(
            WaiterAssignmentService(mutableListOf(first, second)),
            desk,
            RestaurantType.EUROPEAN,
        )

        withRegisteredDriver(desk) {
            service.serve(emptyList(), subUnits())
        }

        assertEquals(0, first.remaining(ActionType.SERVING))
        assertEquals(Waiter.ACTION_LIMIT - 2, second.remaining(ActionType.SERVING))
        assertTrue(desk.getReady().isEmpty())
    }

    @Test
    fun servingStatusCountsDistinctWaitersAndIncludesDeliveryHandoff() {
        val recipe = basicRecipe(1)
        val casualGroup = casual(3, 1)
        val casualMeals = mutableListOf(cookedMeal(casualGroup.members()[0], recipe))
        val casualWaiter = Waiter()
        casualWaiter.consume(ActionType.SERVING, Waiter.ACTION_LIMIT - 1)
        val visit = visitWith(casualGroup, listOf(casualWaiter), casualMeals)

        val deliveryGroup = casual(9, 1, deliveryDistance = 3)
        val deliveryOrder = Order(
            deliveryGroup,
            RESTAURANT_ID,
            deliveryGroup.id(),
            1,
            true,
            mutableListOf(cookedMeal(deliveryGroup.members()[0], recipe)),
        )
        val desk = DeliveryDesk(mutableListOf(), RESTAURANT_ID)
        desk.enqueue(deliveryOrder)
        desk.readyOrder(deliveryOrder)
        val deliveryWaiter = Waiter()
        val service = ServingService(
            WaiterAssignmentService(mutableListOf(casualWaiter, deliveryWaiter)),
            desk,
            RestaurantType.EUROPEAN,
        )
        val log = captureLog()

        withRegisteredDriver(desk) {
            service.serve(listOf(visit), subUnits())
        }

        assertTrue(
            logLines(log).any {
                it.contains("FOH Serving Status") && it.contains("2 waitstaff served 2 meals")
            },
        )
    }

    @Test
    fun tableWithoutServableMealsIsSkippedWithoutError() {
        val group = casual(3, 1)
        val visit = visitWith(group, listOf(Waiter()), mutableListOf())
        val service = serviceWith()

        service.serve(listOf(visit), subUnits())

        assertFalse(visit.isFinished())
    }
}
