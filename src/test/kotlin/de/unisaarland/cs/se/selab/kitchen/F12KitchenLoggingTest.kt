package de.unisaarland.cs.se.selab.kitchen

/* The six kitchen log lines and the cooked statistic - all missing today. */

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F12KitchenLoggingTest {

    private val rice = Ingredient("rice", UnitType.G, 100, 5)

    private fun dish(id: Int, riceAmount: Int = 0) = Recipe(
        id,
        "dish$id",
        20,
        setOf(CookType.EXEC),
        if (riceAmount == 0) mutableListOf() else mutableListOf(RecipeIngredient(rice, riceAmount)),
        null,
    )

    private fun kitchen(pantry: Pantry = Pantry(restaurantId = RESTAURANT_ID), execCooks: Int = 1): Kitchen {
        val roaster = CookRoaster(mapOf(CookType.EXEC to execCooks), restaurantId = RESTAURANT_ID)
        roaster.initialiseCooks()
        return Kitchen(
            roaster,
            pantry,
            mutableListOf(),
            ReservationBook(TableAssignmentService(mutableListOf())),
            RestaurantType.EUROPEAN,
            // RESTAURANT_ID,   <- uncomment after Fix1
        )
    }

    /** An order of [size] meals of [recipe], placed in the current tick. */
    private fun order(group: CustomerGroup, recipe: Recipe): Order {
        val meals = group.members().map { Meal(null, it, recipe) }.toMutableList()
        val order = Order(group, RESTAURANT_ID, group.id(), GlobalClock.currentTick, false, meals)
        meals.forEach { it.orderId = order.getId() }
        return order
    }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** Starting a dish writes a Kitchen Dish Assignment line. */
    @Test
    fun startingToCookIsReported() {
        startEvening()
        val kitchen = kitchen()
        val order = order(regular(1, 2), dish(1))
        kitchen.enqueue(order)

        val log = Fixtures.captureLog()
        kitchen.cook()

        assertTrue(
            Fixtures.logLines(log).contains(
                "[IMPORTANT] Kitchen Dish Assignment (R $RESTAURANT_ID): Cook 1 of type EXEC starts cooking " +
                    "2 meals of dish dish1 based on order ${order.getId()} for orders ${order.getId()}.",
            ),
            "no Dish Assignment line was written: ${Fixtures.logLines(log)}",
        )
    }

    /** Finishing a dish writes a Kitchen Meal Cooked line with the waiting time. */
    @Test
    fun finishingADishIsReportedWithTheWaitingTime() {
        startEvening()
        val kitchen = kitchen()
        kitchen.enqueue(order(regular(1, 2), dish(1)))
        kitchen.cook()

        GlobalClock.advanceTick()
        val log = Fixtures.captureLog()
        kitchen.cook()

        assertTrue(
            Fixtures.logLines(log).contains(
                "[IMPORTANT] Kitchen Meal Cooked (R $RESTAURANT_ID): Cook 1 finished cooking 2 meals of dish dish1 " +
                    "1 ticks after ordering.",
            ),
            "no Meal Cooked line was written: ${Fixtures.logLines(log)}",
        )
    }

    /** Every tick ends with a Kitchen Status line. */
    @Test
    fun theKitchenReportsItsStatusEveryTick() {
        startEvening()
        val kitchen = kitchen()
        kitchen.enqueue(order(regular(1, 2), dish(1)))

        val log = Fixtures.captureLog()
        kitchen.cook()

        assertTrue(
            Fixtures.logLines(log).contains(
                "[DEBUG] Kitchen Status (R $RESTAURANT_ID): 1 cooks were active cooking 2 and finishing 0 meals. " +
                    "0 meals can be served by the waitstaff.",
            ),
            "no Kitchen Status line was written: ${Fixtures.logLines(log)}",
        )
    }

    /**
     * The kitchen reports how many meals it finished this tick; the restaurant books that number
     * into the statistics, so this is the number the "cooked N meals" line is built from.
     */
    @Test
    fun theKitchenReportsHowManyMealsItFinished() {
        startEvening()
        val kitchen = kitchen()
        kitchen.enqueue(order(regular(1, 2), dish(1)))

        val firstTick = kitchen.cook()
        GlobalClock.advanceTick()
        val secondTick = kitchen.cook()

        assertEquals(2, firstTick + secondTick, "both meals have to be reported as finished")
    }

    /** The preparation reports what was thrown out and what was bought. */
    @Test
    fun thePreparationPhaseReportsWhatWasThrownOutAndBought() {
        startEvening()
        val expired = Ingredient("rice", UnitType.G, 100, 0)
        val pantry = Pantry(mutableListOf(expired to 300), restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(dish(1, riceAmount = 50)), pantry, kitchen)

        val log = Fixtures.captureLog()
        kitchen.planEvening(mutableListOf(), 10, menu)

        val lines = Fixtures.logLines(log)
        assertTrue(
            lines.contains("[DEBUG] Pantry (R $RESTAURANT_ID): Removed 300 g of rice from the pantry."),
            "the expired rice was not reported: $lines",
        )
        val procured = "[DEBUG] Pantry (R $RESTAURANT_ID): Procured"
        assertTrue(
            lines.any { it.startsWith(procured) && it.endsWith("of rice from the supplier.") },
            "the procurement was not reported: $lines",
        )
        assertTrue(
            lines.contains("[INFO] Pantry (R $RESTAURANT_ID): Restocked ingredients."),
            "the restocking was not reported: $lines",
        )
    }
}
