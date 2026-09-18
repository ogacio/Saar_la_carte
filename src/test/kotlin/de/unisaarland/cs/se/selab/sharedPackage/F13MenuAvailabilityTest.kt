package de.unisaarland.cs.se.selab.sharedPackage

/* F13: the menu follows the pantry, the kitchen staff and the incidents of the evening. */

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F13MenuAvailabilityTest {

    private val rice = Ingredient("rice", UnitType.G, 100, 5)

    private fun dish(id: Int, amount: Int, cookType: CookType = CookType.EXEC) =
        Recipe(id, "dish$id", 10, setOf(cookType), mutableListOf(RecipeIngredient(rice, amount)), null)

    private fun kitchenOf(pantry: Pantry, staff: Map<CookType, Int>) = Kitchen(
        CookRoaster(staff, restaurantId = RESTAURANT_ID).also { it.initialiseCooks() },
        pantry,
        mutableListOf(),
        ReservationBook(TableAssignmentService(mutableListOf())),
        RestaurantType.EUROPEAN,
    )

    /** A dish the pantry cannot cover comes back on the menu once the supplier delivered. */
    @Test
    fun aDishIsOrderableAgainAfterTheIngredientsWereRestocked() {
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        val soup = dish(1, amount = 50)
        val menu = Menu(mutableListOf(soup), pantry, kitchenOf(pantry, mapOf(CookType.EXEC to 1)))
        menu.refresh()
        assertTrue(menu.getOrderables().isEmpty(), "an empty pantry cannot cover anything")

        pantry.restock(rice, 100)
        menu.refresh()

        assertEquals(listOf(soup), menu.getOrderables())
    }

    /** Reserving the last portion takes the dish off the menu for the next customer. */
    @Test
    fun reservingTheLastPortionTakesTheDishOffTheMenu() {
        val pantry = Pantry(mutableListOf(rice to 50), restaurantId = RESTAURANT_ID)
        val soup = dish(1, amount = 50)
        val menu = Menu(mutableListOf(soup), pantry, kitchenOf(pantry, mapOf(CookType.EXEC to 1)))
        menu.refresh()
        assertEquals(listOf(soup), menu.getOrderables())

        pantry.reserve(soup)
        menu.refresh()

        assertTrue(menu.getOrderables().isEmpty(), "the reserved portion is gone from the pantry")
    }

    /** A dish nobody can cook becomes orderable when a cook of that type is hired. */
    @Test
    fun aDishIsOrderableAfterACookOfItsTypeWasHired() {
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = RESTAURANT_ID)
        val cake = dish(1, amount = 50, cookType = CookType.PASTRY)
        val kitchen = kitchenOf(pantry, mapOf(CookType.EXEC to 1))
        val menu = Menu(mutableListOf(cake), pantry, kitchen)
        menu.refresh()
        assertTrue(menu.getOrderables().isEmpty(), "nobody can bake it yet")

        kitchen.changeStaff(CookType.PASTRY, 1)
        menu.refresh()

        assertEquals(listOf(cake), menu.getOrderables())
    }

    /** Losing the last cook of a type takes the dish off the menu again. */
    @Test
    fun aDishLeavesTheMenuWhenItsLastCookIsGone() {
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = RESTAURANT_ID)
        val cake = dish(1, amount = 50, cookType = CookType.PASTRY)
        val kitchen = kitchenOf(pantry, mapOf(CookType.PASTRY to 1))
        val menu = Menu(mutableListOf(cake), pantry, kitchen)
        menu.refresh()
        assertEquals(listOf(cake), menu.getOrderables())

        kitchen.changeStaff(CookType.PASTRY, -1)
        menu.refresh()

        assertTrue(menu.getOrderables().isEmpty(), "the dish stayed on the menu without a cook")
    }

    /** A recipe change that raises the amount can push a dish off the menu. */
    @Test
    fun aRecipeChangeThatRaisesTheAmountCanTakeADishOffTheMenu() {
        val pantry = Pantry(mutableListOf(rice to 100), restaurantId = RESTAURANT_ID)
        val soup = dish(1, amount = 100)
        val menu = Menu(mutableListOf(soup), pantry, kitchenOf(pantry, mapOf(CookType.EXEC to 1)))
        menu.refresh()
        assertEquals(listOf(soup), menu.getOrderables())

        soup.ingredients.first().adaptBy(50)
        menu.refresh()

        assertTrue(menu.getOrderables().isEmpty(), "150 g cannot be covered by 100 g")
    }
}
