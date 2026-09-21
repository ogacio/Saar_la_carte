package de.unisaarland.cs.se.selab.sharedPackage

/* Pantry and Supplier: shelf life, package sizes and unavailability. */

import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.Supplier
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class F08PantryAndSupplierBugTest {

    private fun rice(bestUntil: Int, packagingVolume: Int = 100) =
        Ingredient("rice", UnitType.G, packagingVolume, bestUntil)

    /** Two deliveries of one ingredient have the same shelf life and expire together. */
    @Test
    fun twoDeliveriesOfTheSameIngredientAgeAtTheSameSpeed() {
        val rice = rice(bestUntil = 3)
        val pantry = Pantry(mutableListOf(rice to 100, rice to 200), restaurantId = RESTAURANT_ID)

        pantry.checkDateAndCleanOut()
        pantry.checkDateAndCleanOut()

        assertEquals(
            300,
            pantry.getTotalIngredients(rice),
            "both deliveries have three evenings of shelf life, neither may be thrown out after two",
        )
    }

    /** Control: one delivery survives its shelf life and is then thrown out. */
    @Test
    fun anIngredientIsThrownOutWhenItsShelfLifeIsOver() {
        val rice = rice(bestUntil = 3)
        val pantry = Pantry(mutableListOf(rice to 100), restaurantId = RESTAURANT_ID)

        repeat(2) { pantry.checkDateAndCleanOut() }
        assertEquals(100, pantry.getTotalIngredients(rice), "still fresh after two evenings")

        pantry.checkDateAndCleanOut()

        assertEquals(0, pantry.getTotalIngredients(rice), "gone in the third evening")
    }

    /** The supplier delivers whole packages, so packages times the package size. */
    @Test
    fun theSupplierDeliversWholePackages() {
        val rice = rice(bestUntil = 5, packagingVolume = 100)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)

        Supplier.resupply(pantry, mapOf(rice to 150))

        assertEquals(200, pantry.getTotalIngredients(rice), "two packages of 100 g, not 2 * 150 g")
    }

    /** An ingredient blocked by an incident is not delivered while the block lasts. */
    @Test
    fun anUnavailableIngredientIsNotDelivered() {
        val rice = rice(bestUntil = 5)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        Supplier.markUnavailable(rice, GlobalClock.getEvening(), 2)

        Supplier.resupply(pantry, mapOf(rice to 100))

        assertEquals(0, pantry.getTotalIngredients(rice), "rice is unavailable this evening")
    }

    /** Once the block is over the ingredient is delivered again. */
    @Test
    fun anIngredientIsBackAfterTheBlockIsOver() {
        val rice = rice(bestUntil = 5)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        Supplier.markUnavailable(rice, GlobalClock.getEvening(), 1)

        GlobalClock.advanceEvening()
        Supplier.resupply(pantry, mapOf(rice to 100))

        assertTrue(pantry.getTotalIngredients(rice) > 0, "the block was over, rice must be delivered again")
    }
}
