package de.unisaarland.cs.se.selab.sharedPackage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * F08: one delivery in the pantry. Each delivery keeps its own remaining amount and its own shelf
 * life, and records whether it has been opened, because "open packages are given priority,
 * followed by packages with the earliest best-before date".
 */
class F08StockEntryTest {

    private fun entry(amount: Int = 50, bestUntil: Int = 2) =
        StockEntry(Ingredient("rice", UnitType.G, 50, bestUntil), amount, bestUntil)

    @Test
    fun takeGivesWhatItCanAndMarksTheDeliveryOpen() {
        val e = entry(amount = 50)
        assertFalse(e.opened)

        assertEquals(20, e.take(20))

        assertEquals(30, e.amount)
        assertTrue(e.opened)
        assertFalse(e.isEmpty())
    }

    @Test
    fun takeIsCappedByWhatIsLeft() {
        val e = entry(amount = 10)

        assertEquals(10, e.take(25), "it can only give what it holds")

        assertEquals(0, e.amount)
        assertTrue(e.isEmpty(), "an emptied delivery is dropped from the pantry")
    }

    @Test
    fun takingNothingLeavesTheDeliveryUntouchedAndUnopened() {
        val e = entry(amount = 50)

        assertEquals(0, e.take(0))
        assertEquals(0, e.take(-5))

        assertEquals(50, e.amount)
        assertFalse(e.opened, "a delivery nobody drew from stays sealed")
    }

    @Test
    fun anEmptiedDeliveryGivesNothingMore() {
        val e = entry(amount = 5)
        e.take(5)

        assertEquals(0, e.take(1))
        assertTrue(e.isEmpty())
    }

    @Test
    fun bestBeforeTwoLastsExactlyTwoEvenings() {
        val e = entry(bestUntil = 2)

        assertTrue(e.ageByOneEvening(), "still good for the second evening")
        assertEquals(1, e.bestUntil)
        assertFalse(e.ageByOneEvening(), "thrown out before the third")
        assertEquals(0, e.bestUntil)
    }
}
