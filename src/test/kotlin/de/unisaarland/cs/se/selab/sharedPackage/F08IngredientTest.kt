package de.unisaarland.cs.se.selab.sharedPackage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * F08, the supplier side of an ingredient: how many whole packages cover an amount, what a
 * PACKAGING incident may change it to, and the shelf-life countdown the pantry drives.
 */
class F08IngredientTest {

    private fun rice(packagingVolume: Int = 50, bestUntil: Int = 3) =
        Ingredient("rice", UnitType.G, packagingVolume, bestUntil)

    @Test
    fun packagesForRoundsPartialPackagesUp() {
        val i = rice(packagingVolume = 50)

        assertEquals(1, i.packagesFor(1), "any amount at all needs a whole package")
        assertEquals(1, i.packagesFor(50), "an exact multiple needs no extra package")
        assertEquals(2, i.packagesFor(51), "one gram over the package opens the next one")
        assertEquals(2, i.packagesFor(100))
    }

    @Test
    fun packagesForBuysNothingForNonPositiveAmounts() {
        val i = rice()

        assertEquals(0, i.packagesFor(0))
        assertEquals(0, i.packagesFor(-10), "a surplus never turns into a negative order")
    }

    @Test
    fun aPackagingIncidentChangesHowManyPackagesAnAmountNeeds() {
        val i = rice(packagingVolume = 50)
        assertEquals(2, i.packagesFor(60))

        i.changePackaging(30)

        assertEquals(30, i.packagingVolume)
        assertEquals(2, i.packagesFor(60), "60 g now fits in two 30 g packages exactly")
        assertEquals(3, i.packagesFor(61))
    }

    @Test
    fun aPackagingIncidentIsIgnoredWhenTheNewVolumeIsNotPositive() {
        val i = rice(packagingVolume = 50)

        i.changePackaging(0)
        assertEquals(50, i.packagingVolume, "a zero volume would make packagesFor divide by zero")

        i.changePackaging(-5)
        assertEquals(50, i.packagingVolume)
    }

    @Test
    fun reduceBestUntilCountsDownAndStopsAtZero() {
        val i = rice(bestUntil = 2)

        assertTrue(i.reduceBestUntil())
        assertEquals(1, i.bestUntil)
        assertTrue(i.reduceBestUntil())
        assertEquals(0, i.bestUntil)
        assertFalse(i.reduceBestUntil(), "an exhausted shelf life does not go negative")
        assertEquals(0, i.bestUntil)
    }

    @Test
    fun everyRecipeIngredientRegistersItselfSoARecipeIncidentReachesThemAll() {
        val i = rice()
        val first = RecipeIngredient(i, 10)
        val second = RecipeIngredient(i, 20)

        assertEquals(2, i.usages().size)
        assertSame(first, i.usages()[0])
        assertSame(second, i.usages()[1])
    }
}
