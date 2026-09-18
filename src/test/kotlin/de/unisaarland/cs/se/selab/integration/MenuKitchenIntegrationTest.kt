package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration of the menu (F13) with the real pantry, kitchen and cooks: what is orderable follows
 * the pantry stock and the kitchen staff.
 */
class MenuKitchenIntegrationTest {

    private val rice = Ingredient("rice", UnitType.G, 100, 3)

    private fun dish(id: Int, riceAmount: Int, vararg cookTypes: CookType) =
        Recipe(id, "dish$id", 10, cookTypes.toSet(), mutableListOf(RecipeIngredient(rice, riceAmount)), null)

    private fun kitchen(pantry: Pantry, staff: Map<CookType, Int>): Kitchen {
        val roaster = CookRoaster(staff, restaurantId = 1).also { it.initialiseCooks() }
        return Kitchen(
            roaster,
            pantry,
            mutableListOf(),
            ReservationBook(TableAssignmentService(mutableListOf())),
            RestaurantType.EUROPEAN,
        )
    }

    @Test
    fun dishWithoutAnyEligibleCookIsNotOrderable() {
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = 1)
        val execDish = dish(1, 100, CookType.EXEC)
        val pastryDish = dish(2, 100, CookType.PASTRY)
        val menu = Menu(mutableListOf(execDish, pastryDish), pantry, kitchen(pantry, mapOf(CookType.EXEC to 1)))

        menu.refresh()

        assertEquals(listOf(execDish), menu.getOrderables())
    }

    @Test
    fun reservingIngredientsForAnOrderRemovesDishesThePantryCanNoLongerCover() {
        val pantry = Pantry(mutableListOf(rice to 150), restaurantId = 1)
        val small = dish(1, 50, CookType.EXEC)
        val large = dish(2, 120, CookType.EXEC)
        val menu = Menu(mutableListOf(small, large), pantry, kitchen(pantry, mapOf(CookType.EXEC to 1)))
        menu.refresh()
        assertEquals(listOf(small, large), menu.getOrderables())

        assertTrue(pantry.reserve(large))
        menu.refresh()

        assertEquals(emptyList(), menu.getOrderables())
    }

    @Test
    fun staffChangeAddingAnEligibleCookMakesTheDishOrderable() {
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = 1)
        val pastryDish = dish(1, 100, CookType.PASTRY)
        val kitchen = kitchen(pantry, mapOf(CookType.EXEC to 1))
        val menu = Menu(mutableListOf(pastryDish), pantry, kitchen)
        menu.refresh()
        assertTrue(menu.getOrderables().isEmpty())

        kitchen.changeStaff(CookType.PASTRY, 1)
        menu.refresh()

        assertEquals(listOf(pastryDish), menu.getOrderables())
    }

    /**
     * Spec: "A dish can be ordered exactly if the required ingredients are available in the pantry
     * and at least one eligible cook exists in the restaurant." A busy cook still exists.
     */
    @Test
    fun dishStaysOrderableWhileItsOnlyCookIsBusy() {
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = 1)
        val execDish = dish(1, 100, CookType.EXEC)
        val kitchen = kitchen(pantry, mapOf(CookType.EXEC to 1))
        val menu = Menu(mutableListOf(execDish), pantry, kitchen)
        kitchen.roaster.startCooking(mutableListOf(Meal(1, Customer(null), execDish)))

        menu.refresh()

        assertEquals(listOf(execDish), menu.getOrderables())
    }
}
