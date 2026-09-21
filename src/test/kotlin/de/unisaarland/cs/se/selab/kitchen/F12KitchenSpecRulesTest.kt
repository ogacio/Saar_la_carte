package de.unisaarland.cs.se.selab.kitchen

/* Three kitchen rules we implement differently from the specification. */

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
import de.unisaarland.cs.se.selab.testsupport.Fixtures
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F12KitchenSpecRulesTest {

    private fun dish(id: Int, minutes: Int = 10, vararg cookTypes: CookType) =
        Recipe(id, "dish$id", minutes, cookTypes.toSet(), mutableListOf(), null)

    private fun meals(recipe: Recipe, count: Int = 1): MutableList<Meal> =
        MutableList(count) { Meal(1, Customer(null), recipe) }

    private fun roaster(staff: Map<CookType, Int>) =
        CookRoaster(staff, restaurantId = RESTAURANT_ID).also { it.initialiseCooks() }

    private fun kitchen(staff: Map<CookType, Int>) = Kitchen(
        roaster(staff),
        Pantry(restaurantId = RESTAURANT_ID),
        mutableListOf(),
        ReservationBook(TableAssignmentService(mutableListOf())),
        RestaurantType.EUROPEAN,
        // RESTAURANT_ID,   <- uncomment after Fix1
    )

    private fun order(id: Int, recipe: Recipe, count: Int = 1): Order {
        val group = regular(id, count)
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, id, GlobalClock.currentTick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    // ---------------------------------------------------------------- duration

    /** A ten minute dish is a duration of 0 ticks, so it is finished in the tick it started. */
    @Test
    fun aTenMinuteDishIsFinishedInTheTickItWasStarted() {
        startEvening()
        val cook = Cook(CookType.SOUS)
        val batch = meals(dish(1, minutes = 10, CookType.SOUS))

        cook.startCooking(batch)

        // cookingFinished() empties the list it was given, so the returned batch is the one to read
        val finished = cook.cookingFinished()
        assertEquals(1, finished?.size, "ten minutes is a duration of 0 ticks")
        assertEquals(listOf(MealStatus.COOKED), finished.orEmpty().map { it.status })
    }

    /** A thirty minute dish is finished two ticks after it was started. */
    @Test
    fun aThirtyMinuteDishIsFinishedTwoTicksAfterItWasStarted() {
        startEvening()
        val cook = Cook(CookType.SOUS)
        cook.startCooking(meals(dish(1, minutes = 30, CookType.SOUS)))

        GlobalClock.advanceTick()
        assertNull(cook.cookingFinished(), "still cooking one tick later")
        GlobalClock.advanceTick()

        assertEquals(1, cook.cookingFinished()?.size, "finished two ticks after it started")
    }

    // ------------------------------------------------------------ cook ranking

    /** The dish is assigned to the lowest-ranking eligible cook. */
    @Test
    fun theLowestRankingEligibleCookTakesTheDish() {
        startEvening()
        val roaster = roaster(mapOf(CookType.EXEC to 1, CookType.PASTRY to 1))

        val cook = roaster.startCooking(meals(dish(1, 10, CookType.EXEC, CookType.PASTRY)))

        assertEquals(CookType.PASTRY, assertNotNull(cook).getType(), "the most junior eligible cook cooks")
    }

    /** The ranking also decides between two specialised cooks. */
    @Test
    fun theRankingAlsoHoldsAmongTheSpecialists() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SAUCE to 1, CookType.VEGETABLE to 1))

        val cook = roaster.startCooking(meals(dish(1, 10, CookType.SAUCE, CookType.VEGETABLE)))

        assertEquals(CookType.VEGETABLE, assertNotNull(cook).getType())
    }

    /** A busy junior cook is skipped, the dish goes up the hierarchy. */
    @Test
    fun aBusyJuniorCookDoesNotBlockTheDish() {
        startEvening()
        val roaster = roaster(mapOf(CookType.EXEC to 1, CookType.PASTRY to 1))
        val cookable = dish(1, 60, CookType.EXEC, CookType.PASTRY)
        roaster.startCooking(meals(cookable))

        val second = roaster.startCooking(meals(dish(2, 60, CookType.EXEC, CookType.PASTRY)))

        assertEquals(CookType.EXEC, assertNotNull(second).getType(), "the busy PASTRY cook is skipped")
    }

    // --------------------------------------------------------- one recipe at a time

    /** A cook starts another recipe only in the tick after it finished one. */
    @Test
    fun aCookMayOnlyStartAnotherRecipeInTheSubsequentTick() {
        startEvening()
        val kitchen = kitchen(mapOf(CookType.SOUS to 1))
        val first = order(1, dish(1, 10, CookType.SOUS))
        val second = order(2, dish(2, 10, CookType.SOUS))
        kitchen.enqueue(first)
        kitchen.enqueue(second)

        kitchen.cook()

        assertEquals(listOf(MealStatus.COOKED), first.getMeals().map { it.status }, "ten minutes, same tick")
        assertEquals(
            listOf(MealStatus.QUEUED),
            second.getMeals().map { it.status },
            "the cook may only start the next recipe in the following tick",
        )
    }

    // ------------------------------------------------------------- the log lines

    /** The assignment line comes before the finished line. */
    @Test
    fun theDishAssignmentIsLoggedBeforeTheMealThatFinished() {
        startEvening()
        val kitchen = kitchen(mapOf(CookType.SOUS to 1))
        kitchen.enqueue(order(1, dish(1, 10, CookType.SOUS)))

        val log = Fixtures.captureLog()
        kitchen.cook()

        val lines = Fixtures.logLines(log)
        val assignment = lines.indexOfFirst { it.contains("Kitchen Dish Assignment") }
        val cooked = lines.indexOfFirst { it.contains("Kitchen Meal Cooked") }
        assertTrue(assignment >= 0 && cooked >= 0, "both lines have to be written: $lines")
        assertTrue(assignment < cooked, "the assignment comes first: $lines")
    }

    /** The finished meals are reported in ascending cook id. */
    @Test
    fun theFinishedMealsAreLoggedInAscendingCookId() {
        startEvening()
        val kitchen = kitchen(mapOf(CookType.SOUS to 1, CookType.PASTRY to 1))
        // two dishes, each cookable by exactly one of the two cooks, so both cooks work this tick
        kitchen.enqueue(order(1, dish(1, 10, CookType.PASTRY)))
        kitchen.enqueue(order(2, dish(2, 10, CookType.SOUS)))

        val log = Fixtures.captureLog()
        kitchen.cook()

        val cookIds = Fixtures.logLines(log)
            .filter { it.contains("Kitchen Meal Cooked") }
            .map { it.substringAfter("Cook ").substringBefore(" ").toInt() }
        assertEquals(listOf(1, 2), cookIds, "the finished meals are reported in ascending cook id")
    }

    /** The status counts the meals finished this tick as cooking too. */
    @Test
    fun theKitchenStatusCountsTheMealsThatFinishedThisTick() {
        startEvening()
        val kitchen = kitchen(mapOf(CookType.SOUS to 1))
        kitchen.enqueue(order(1, dish(1, 10, CookType.SOUS)))

        val log = Fixtures.captureLog()
        kitchen.cook()

        assertTrue(
            Fixtures.logLines(log).contains(
                "[DEBUG] Kitchen Status (R $RESTAURANT_ID): 1 cooks were active cooking 1 and finishing 1 meals. " +
                    "1 meals can be served by the waitstaff.",
            ),
            "the finished meal was not counted as cooking: ${Fixtures.logLines(log)}",
        )
    }
}
