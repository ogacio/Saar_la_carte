package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * F09: the visit history the kitchen plans from. "For those known regulars planning to visit on
 * this evening, they plan all ingredients to be able to cook all orders of the group's last three
 * visits. In case there are less than 3 visits, the kitchen considers all of those visits."
 */
class F09HistoryTest {

    private val soup = recipe(1)
    private val stew = recipe(2)
    private val pie = recipe(3)
    private val tart = recipe(4)

    private fun historyOf(vararg visits: List<Recipe>) = History().apply {
        visits.forEach { shiftAndPutNew(it) }
    }

    @Test
    fun aFreshGroupIsNeitherKnownNorHasOrdered() {
        val h = History()

        assertFalse(h.isKnown(), "nobody has seen this group yet")
        assertFalse(h.hasOrdered())
        assertTrue(h.getLastThree().isEmpty())
    }

    @Test
    fun fewerThanThreeVisitsAreAllKept() {
        val h = historyOf(listOf(soup), listOf(stew))

        assertEquals(listOf(listOf(soup), listOf(stew)), h.getLastThree())
        assertTrue(h.isKnown())
        assertTrue(h.hasOrdered())
    }

    @Test
    fun theWindowHoldsExactlyThreeVisits() {
        val h = historyOf(listOf(soup), listOf(stew), listOf(pie))

        assertEquals(3, h.getLastThree().size)
        assertEquals(listOf(listOf(soup), listOf(stew), listOf(pie)), h.getLastThree())
    }

    @Test
    fun aFourthVisitPushesTheOldestOneOut() {
        val h = historyOf(listOf(soup), listOf(stew), listOf(pie), listOf(tart))

        assertEquals(3, h.getLastThree().size)
        assertEquals(
            listOf(listOf(stew), listOf(pie), listOf(tart)),
            h.getLastThree(),
            "the kitchen must stop planning the dish of the dropped visit",
        )
    }

    @Test
    fun onlyTheMostRecentThreeSurviveManyVisits() {
        val h = History()
        repeat(5) { h.shiftAndPutNew(listOf(soup)) }
        h.shiftAndPutNew(listOf(stew))
        h.shiftAndPutNew(listOf(pie))

        assertEquals(listOf(listOf(soup), listOf(stew), listOf(pie)), h.getLastThree())
    }

    @Test
    fun oneVisitCanCoverSeveralDishesAtOnce() {
        val h = historyOf(listOf(soup, soup, stew))

        assertEquals(listOf(listOf(soup, soup, stew)), h.getLastThree())
        assertTrue(h.hasOrdered())
    }

    @Test
    fun aVisitWithoutAnyDishCountsAsKnownButNotAsHavingOrdered() {
        val h = historyOf(emptyList())

        assertTrue(h.isKnown(), "the group has been here")
        assertFalse(h.hasOrdered(), "forum 328: it is only planned for once it has ordered")
    }

    @Test
    fun oneOrderingVisitAmongEmptyOnesIsEnoughToBePlannedFor() {
        val h = historyOf(emptyList(), listOf(soup), emptyList())

        assertTrue(h.hasOrdered())
    }
}
