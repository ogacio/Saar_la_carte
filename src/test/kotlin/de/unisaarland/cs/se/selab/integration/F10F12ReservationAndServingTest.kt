package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.dish
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.kitchen
import de.unisaarland.cs.se.selab.testsupport.KitchenBoundaryFixtures.startEvening
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** F10/F12: real ordering reserves stock; kitchen output reaches the real serving service. */
class F10F12ReservationAndServingTest {
    private val rice = Ingredient("rice", UnitType.G, 100, 5)
    private val salt = Ingredient("salt", UnitType.G, 100, 5)

    @Test
    fun placingAnOrderReservesExactlyItsMealsAndCookingDoesNotChargeStockAgain() {
        startEvening()
        val reserved = mutableListOf<Pair<Ingredient, Int>>()
        val pantry = Pantry(reserved = reserved, restaurantId = 1)
        pantry.restock(rice, 100)
        pantry.restock(salt, 100)
        val recipe = dish(1, minutes = 11).also { it.ingredients.add(RecipeIngredient(rice, 20)) }
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(recipe), pantry, kitchen)
        val sbu = SubUnits(1, menu, pantry, kitchen)
        val waitstaff = WaiterAssignmentService(mutableListOf(Waiter()))
        val visit = Visit(casual(1, 2))
        val waiter = requireNotNull(waitstaff.assignPermanent(2))
        visit.seated(Table(1, 2, TableType.COMMON), listOf(waiter), GlobalClock.currentTick)
        OrderingService(waitstaff, RestaurantType.ASIAN).takeOrder(visit, sbu, GlobalClock.currentTick)

        assertSame(visit.order, kitchen.queue.single())
        assertEquals(List(2) { rice to 20 }, reserved)
        assertEquals(60, pantry.getTotalIngredients(rice))
        assertEquals(100, pantry.getTotalIngredients(salt))
        menu.refresh()
        assertTrue(recipe in menu.getOrderables())
        val serving = ServingService(waitstaff, DeliveryDesk(mutableListOf(), 1), RestaurantType.ASIAN)
        assertEquals(0, kitchen.cook())
        serving.serve(listOf(visit), sbu)
        assertEquals(List(2) { CustomerStatus.ORDERED }, visit.group.members().map { it.status() })

        GlobalClock.advanceTick()
        assertEquals(2, kitchen.cook())
        assertEquals(List(2) { MealStatus.COOKED }, requireNotNull(visit.order).getMeals().map { it.status })
        serving.serve(listOf(visit), sbu)
        assertEquals(List(2) { CustomerStatus.SERVED }, visit.group.members().map { it.status() })
        assertEquals(60, pantry.getTotalIngredients(rice))
        assertEquals(100, pantry.getTotalIngredients(salt))
        assertTrue(reserved.isEmpty())
    }

    /** Spec: the cook takes only its own batch's reserved ingredients when cooking starts. */
//    @Test
//    fun startingABatchConsumesItsReservationsButPreservesTheWaitingOrdersReservation() {
//        startEvening()
//        val reserved = mutableListOf<Pair<Ingredient, Int>>()
//        val pantry = Pantry(reserved = reserved, restaurantId = 1)
//        pantry.restock(rice, 100)
//        val slow = dish(1, minutes = 30).also { it.ingredients.add(RecipeIngredient(rice, 20)) }
//        val waiting = dish(2).also { it.ingredients.add(RecipeIngredient(rice, 10)) }
//        val kitchen = kitchen(pantry)
//        assertTrue(pantry.reserve(slow))
//        assertTrue(pantry.reserve(waiting))
//        kitchen.enqueue(order(slow, waiting))
//
//        kitchen.cook()
//
//        assertEquals(listOf(rice to 10), reserved, "The cooking batch has already taken its ingredients")
//        assertEquals(70, pantry.getTotalIngredients(rice))
//    }
}
