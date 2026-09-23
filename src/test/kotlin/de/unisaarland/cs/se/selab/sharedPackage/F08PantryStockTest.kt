package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * F08, the pantry's own stock keeping: each delivery ages on its own counter, the supplier's
 * deliveries are drawn from in the forum's order ("open packages are given priority, followed by
 * packages with the earliest best-before date"), and reserving is all-or-nothing.
 */
class F08PantryStockTest {

    private fun rice(bestBefore: Int = 5) = Ingredient("rice", UnitType.G, 50, bestBefore)

    private fun dishOf(vararg parts: Pair<Ingredient, Int>) = Recipe(
        1,
        "dish",
        10,
        setOf(CookType.EXEC),
        parts.map { RecipeIngredient(it.first, it.second) }.toMutableList(),
        null,
    )

    @Test
    fun restockingNothingAddsNoDelivery() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)

        pantry.restock(r, 0)
        pantry.restock(r, -20)

        assertEquals(0, pantry.getTotalIngredients(r), "an empty delivery is not stored")
    }

    @Test
    fun theStockIsTheSumOfEveryDelivery() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)

        pantry.restock(r, 50)
        pantry.restock(r, 30)

        assertEquals(80, pantry.getTotalIngredients(r), "deliveries accumulate, they do not replace")
    }

    @Test
    fun reservingIsAllOrNothing() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 10)
        val dish = dishOf(r to 25)

        assertFalse(pantry.canCover(dish))
        assertFalse(pantry.reserve(dish), "a recipe it cannot cover is refused")
        assertEquals(10, pantry.getTotalIngredients(r), "and nothing is taken from the stock")
    }

    @Test
    fun reservingTakesTheIngredientsOutOfTheStock() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 50)
        val dish = dishOf(r to 20)

        assertTrue(pantry.reserve(dish))

        assertEquals(30, pantry.getTotalIngredients(r), "reserved ingredients are no longer available")
    }

    @Test
    fun aRecipeNeedingOneIngredientTwiceNeedsBothAmounts() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 30)

        assertFalse(pantry.canCover(dishOf(r to 20, r to 20)), "20 + 20 does not fit in 30")
        assertTrue(pantry.canCover(dishOf(r to 20, r to 10)))
    }

    @Test
    fun anOpenedDeliveryIsUsedUpBeforeAnUnopenedOneThatExpiresEarlier() {
        val r = rice(bestBefore = 5)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 50) // delivery A, best until 5
        pantry.removeFromStock(r, 10) // opens A, 40 left
        r.reduceBestUntil()
        r.reduceBestUntil()
        pantry.restock(r, 50) // delivery B, best until 3, unopened but expires sooner

        pantry.removeFromStock(r, 10)

        // Age three evenings: B (3) is thrown out, A (5) survives. What B still holds tells us
        // which delivery the second draw came from.
        val log = captureLog()
        repeat(3) { pantry.checkDateAndCleanOut() }
        val removed = logLines(log).filter { it.contains("Removed") }

        assertEquals(1, removed.size, removed.toString())
        assertTrue(
            removed[0].contains("Removed 50 g of rice"),
            "the open delivery A should have been drawn from, leaving B untouched: ${removed[0]}",
        )
    }

    @Test
    fun anExpiredDeliveryIsThrownOutAndReportedOnce() {
        val r = rice(bestBefore = 1)
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 40)

        val log = captureLog()
        pantry.checkDateAndCleanOut()

        assertEquals(0, pantry.getTotalIngredients(r), "it is gone from the stock")
        assertTrue(logLines(log).any { it.contains("Removed 40 g of rice") })
    }

    @Test
    fun discardingTheEveningKeepsTheStockAndOnlyClearsReservations() {
        val r = rice()
        val pantry = Pantry(restaurantId = RESTAURANT_ID)
        pantry.restock(r, 50)
        pantry.reserve(dishOf(r to 20))

        pantry.discardEvening()

        assertEquals(30, pantry.getTotalIngredients(r), "unused ingredients remain in the pantry")
        assertEquals(RESTAURANT_ID, pantry.getRestaurantId())
    }
}
