package de.unisaarland.cs.se.selab.kitchen

/* CookRoaster: staffing, cook ids and the COOK incident. */

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class F11CookRoasterTest {

    private fun dish(id: Int, cookType: CookType = CookType.SOUS, minutes: Int = 10) =
        Recipe(id, "dish$id", minutes, setOf(cookType), mutableListOf(), null)

    private fun meals(recipe: Recipe, count: Int = 1): MutableList<Meal> =
        MutableList(count) { Meal(1, Customer(null), recipe) }

    private fun roaster(staff: Map<CookType, Int>) =
        CookRoaster(staff, restaurantId = RESTAURANT_ID).also { it.initialiseCooks() }

    private fun startEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
    }

    /** The staff of the restaurants file becomes one cook object per cook. */
    @Test
    fun initialiseCooksCreatesOneCookPerStaffEntry() {
        val roaster = roaster(mapOf(CookType.SOUS to 2, CookType.PASTRY to 1))

        assertEquals(3, roaster.cooks.size)
        assertEquals(2, roaster.cooks.count { it.getType() == CookType.SOUS })
        assertEquals(1, roaster.cooks.count { it.getType() == CookType.PASTRY })
    }

    /** A cook that cannot cook the dish is skipped, however free it is. */
    @Test
    fun onlyAnEligibleCookTakesTheBatch() {
        startEvening()
        val roaster = roaster(mapOf(CookType.PASTRY to 1, CookType.EXEC to 1))

        val cook = roaster.startCooking(meals(dish(1, CookType.EXEC)))

        assertEquals(CookType.EXEC, assertNotNull(cook).getType())
    }

    /** Two cooks of one type are interchangeable: one batch each. */
    @Test
    fun twoCooksOfTheSameTypeTakeOneBatchEach() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 2))
        val busy = roaster.startCooking(meals(dish(1, minutes = 60)))

        val second = roaster.startCooking(meals(dish(2, minutes = 60)))

        assertEquals(1, assertNotNull(busy).getId())
        assertEquals(2, assertNotNull(second).getId(), "the second batch went to the other cook")
    }

    /** Every eligible cook busy: the dish waits instead of overloading one. */
    @Test
    fun startCookingReturnsNullWhenEveryEligibleCookIsBusy() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 1))
        roaster.startCooking(meals(dish(1, minutes = 60)))

        assertNull(roaster.startCooking(meals(dish(2, minutes = 60))), "there is no second cook")
    }

    /** A cook keeps its id for the rest of the evening. */
    @Test
    fun aCookKeepsItsIdForTheWholeEvening() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 1))
        val first = roaster.startCooking(meals(dish(1)))

        GlobalClock.advanceTick()
        roaster.finished()
        GlobalClock.advanceTick()
        val second = roaster.startCooking(meals(dish(2)))

        assertEquals(1, assertNotNull(first).getId())
        assertEquals(1, assertNotNull(second).getId(), "the same cook keeps id 1")
    }

    /** Ids are granted at the first dish, so an unused cook has none. */
    @Test
    fun aCookThatNeverCooksHasNoId() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 3))

        roaster.startCooking(meals(dish(1, minutes = 60)))

        assertEquals(listOf(1, null, null), roaster.cooks.map { it.getId() })
    }

    /** A busy cook still exists, so the dish stays cookable. */
    @Test
    fun hasEligibleIsTrueWhileTheOnlyCookIsBusy() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 1))
        val soup = dish(1, minutes = 60)
        roaster.startCooking(meals(soup))

        assertTrue(roaster.hasEligible(soup))
    }

    /** No cook of that type: the dish can never be cooked here. */
    @Test
    fun hasEligibleIsFalseForATypeNobodyCanCook() {
        val roaster = roaster(mapOf(CookType.SOUS to 2))

        assertFalse(roaster.hasEligible(dish(1, CookType.PASTRY)))
    }

    /** A hiring incident adds free, nameless cooks. */
    @Test
    fun changeStaffHiresFreeCooksWithoutAnId() {
        val roaster = roaster(mapOf(CookType.EXEC to 1))

        roaster.changeStaff(CookType.PASTRY, 2)

        assertEquals(2, roaster.cooks.count { it.getType() == CookType.PASTRY })
        assertTrue(roaster.cooks.all { it.isFree() })
        assertTrue(roaster.cooks.filter { it.getType() == CookType.PASTRY }.all { it.getId() == null })
    }

    /** A firing incident only touches the type it names. */
    @Test
    fun changeStaffFiresOnlyCooksOfTheGivenType() {
        val roaster = roaster(mapOf(CookType.SOUS to 2, CookType.PASTRY to 2))

        roaster.changeStaff(CookType.PASTRY, -1)

        assertEquals(2, roaster.cooks.count { it.getType() == CookType.SOUS })
        assertEquals(1, roaster.cooks.count { it.getType() == CookType.PASTRY })
    }

    /** Firing more cooks than there are leaves none, without throwing. */
    @Test
    fun firingMoreCooksThanExistLeavesNone() {
        val roaster = roaster(mapOf(CookType.SOUS to 2))

        roaster.changeStaff(CookType.SOUS, -5)

        assertTrue(roaster.cooks.isEmpty())
    }

    /** The evening reset frees every cook and restarts the ids at 1. */
    @Test
    fun resetEveningFreesEveryCookAndStartsIdsAtOne() {
        startEvening()
        val roaster = roaster(mapOf(CookType.SOUS to 2))
        roaster.startCooking(meals(dish(1, minutes = 60)))

        roaster.resetEvening()

        assertTrue(roaster.cooks.all { it.isFree() }, "the long dish did not survive the night")
        assertEquals(listOf(null, null), roaster.cooks.map { it.getId() }, "the ids are forgotten")

        startEvening()
        val firstOfTheNewEvening = roaster.startCooking(meals(dish(2)))

        assertEquals(1, assertNotNull(firstOfTheNewEvening).getId(), "ids start again at 1")
    }
}
