package de.unisaarland.cs.se.selab.kitchen

/* Kitchen.planEvening(): what the kitchen expects and therefore buys. */

import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.members
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F09KitchenPlanEveningTest {

    /** 50 g of rice per portion; the supplier sells rice in packages of 100 g. */
    private fun rice(bestUntil: Int = 5) = Ingredient("rice", UnitType.G, 100, bestUntil)

    private fun dish(rice: Ingredient, amount: Int = PORTION) =
        Recipe(1, "dish1", 10, setOf(CookType.EXEC), mutableListOf(RecipeIngredient(rice, amount)), null)

    private fun emptyBook() = ReservationBook(TableAssignmentService(mutableListOf()))

    private fun kitchen(pantry: Pantry, book: ReservationBook = emptyBook()) =
        Kitchen(
            CookRoaster(mapOf(CookType.EXEC to 1), restaurantId = RESTAURANT_ID).also { it.initialiseCooks() },
            pantry,
            mutableListOf(),
            book,
            RestaurantType.EUROPEAN,
            // RESTAURANT_ID,   <- uncomment after Fix1
        )

    /** An EVENT group of [size] whose members all want dish1 in a European restaurant. */
    private fun eventGroup(id: Int, size: Int, evening: Int) = EventCustomerGroup(
        id,
        size,
        TableType.COMMON,
        1,
        members(size),
        emptyList(),
        setOf(RestaurantType.EUROPEAN),
        evening,
        mapOf(RestaurantType.EUROPEAN to "dish1"),
    )

    /** A pantry that already covers the evening is left alone. */
    @Test
    fun nothingIsBoughtWhenThePantryAlreadyCoversTheEvening() {
        val rice = rice()
        val pantry = Pantry(mutableListOf(rice to 500), restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(dish(rice)), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), TEN_SEATS, menu)

        assertEquals(500, pantry.getTotalIngredients(rice), "the pantry was full, nothing had to be bought")
    }

    /** Ten seats are one expected group, eleven are two. */
    @Test
    fun tenSeatsAreOneExpectedGroupAndElevenAreTwo() {
        val riceForOne = rice()
        val pantryForOne = Pantry(mutableListOf(riceForOne to PORTION), restaurantId = RESTAURANT_ID)
        val kitchenForOne = kitchen(pantryForOne)
        val menuForOne = Menu(mutableListOf(dish(riceForOne)), pantryForOne, kitchenForOne)

        kitchenForOne.planEvening(mutableListOf(), TEN_SEATS, menuForOne)

        assertEquals(PORTION, pantryForOne.getTotalIngredients(riceForOne), "10 seats need one portion")

        val riceForTwo = rice()
        val pantryForTwo = Pantry(mutableListOf(riceForTwo to PORTION), restaurantId = RESTAURANT_ID)
        val kitchenForTwo = kitchen(pantryForTwo)
        val menuForTwo = Menu(mutableListOf(dish(riceForTwo)), pantryForTwo, kitchenForTwo)

        kitchenForTwo.planEvening(mutableListOf(), TEN_SEATS + 1, menuForTwo)

        assertTrue(
            pantryForTwo.getTotalIngredients(riceForTwo) > PORTION,
            "11 seats are two expected groups, so a second portion has to be bought",
        )
    }

    /** Only the EVENT groups booked for tonight raise the expectation. */
    @Test
    fun theEventGroupsBookedForThisEveningRaiseTheExpectation() {
        GlobalClock.advanceEvening()
        val tonight = GlobalClock.getEvening()

        val quiet = rice()
        val quietPantry = Pantry(mutableListOf(quiet to PORTION), restaurantId = RESTAURANT_ID)
        val quietBook = ReservationBook(TableAssignmentService(mutableListOf()))
        quietBook.bookAhead(eventGroup(1, 4, tonight + 1), tonight + 1)
        val quietKitchen = kitchen(quietPantry, quietBook)
        val quietMenu = Menu(mutableListOf(dish(quiet)), quietPantry, quietKitchen)

        quietKitchen.planEvening(mutableListOf(), TEN_SEATS, quietMenu)

        assertEquals(
            PORTION,
            quietPantry.getTotalIngredients(quiet),
            "the group booked for tomorrow must not be cooked for tonight",
        )

        val busy = rice()
        val busyPantry = Pantry(mutableListOf(busy to PORTION), restaurantId = RESTAURANT_ID)
        val busyBook = ReservationBook(TableAssignmentService(mutableListOf()))
        busyBook.bookAhead(eventGroup(2, 4, tonight), tonight)
        val busyKitchen = kitchen(busyPantry, busyBook)
        val busyMenu = Menu(mutableListOf(dish(busy)), busyPantry, busyKitchen)

        busyKitchen.planEvening(mutableListOf(), TEN_SEATS, busyMenu)

        assertTrue(
            busyPantry.getTotalIngredients(busy) > PORTION,
            "four event guests want dish1, so their ingredients have to be bought",
        )
    }

    /** Expired stock is thrown out before the need is calculated. */
    @Test
    fun expiredStockIsThrownOutBeforeTheNeedIsCalculated() {
        val rice = rice(bestUntil = 0)
        val pantry = Pantry(mutableListOf(rice to 300), restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val menu = Menu(mutableListOf(dish(rice)), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), TEN_SEATS, menu)

        val stock = pantry.getTotalIngredients(rice)
        assertTrue(stock > 0, "the kitchen counted the expired rice as available and bought nothing")
        assertTrue(stock < 300, "the expired rice is still in the pantry")
    }

    /** Every dish on the menu is planned for, not just the first. */
    @Test
    fun everyDishOnTheMenuIsPlannedFor() {
        val rice = rice()
        val noodles = Ingredient("noodles", UnitType.G, 100, 5)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        val kitchen = kitchen(pantry)
        val riceDish = dish(rice)
        val noodleDish = Recipe(
            2,
            "dish2",
            10,
            setOf(CookType.EXEC),
            mutableListOf(RecipeIngredient(noodles, PORTION)),
            null,
        )
        val menu = Menu(mutableListOf(riceDish, noodleDish), pantry, kitchen)

        kitchen.planEvening(mutableListOf(), TEN_SEATS, menu)

        assertTrue(pantry.getTotalIngredients(rice) > 0, "no rice was bought")
        assertTrue(pantry.getTotalIngredients(noodles) > 0, "no noodles were bought")
    }

    private companion object {
        /** One portion of rice. */
        const val PORTION = 50

        /** The seats that make up exactly one expected casual group. */
        const val TEN_SEATS = 10

        // NOT COVERED HERE: the expectation built from the last three visits of a REGULAR group
        // (RegularCustomerGroup.expectedDishes reads the visit history). That needs a group with a
        // history, which only exists after a full visit - better placed in an integration test.
    }
}
