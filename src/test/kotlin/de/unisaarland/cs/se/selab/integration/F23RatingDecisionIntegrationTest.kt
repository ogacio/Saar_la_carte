package de.unisaarland.cs.se.selab.integration

import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.BrowsingService
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import org.mockito.kotlin.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** F23/F25: a casual's actual rating changes the choice of the next group through the rating book. */
class F23RatingDecisionIntegrationTest {
    @BeforeTest
    fun freshEvening() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        captureLog()
        RatingBook.initializeRatings(1, 0, 0)
        RatingBook.initializeRatings(2, 0, 0)
    }

    private fun snapshot(id: Int) = RestaurantData(
        id,
        RestaurantType.EUROPEAN,
        1,
        24,
        listOf(recipe(1)),
        mapOf(TableType.COMMON to 4),
        0,
        false,
        4,
        emptyMap(),
    )

    private fun choiceAfterVisit(likelihood: RatingLikelihood, servedTick: Int): Int? {
        val group = casual(1, 2, likelihood = likelihood)
        val visit = Visit(group).also { it.orderedTick = 1 }
        group.members().forEach { it.receive(Meal(1, it, recipe(1)), servedTick) }
        RatingService().rate(visit, SubUnits(2, mock(), mock(), mock()))
        val browsing = BrowsingService(mutableListOf(snapshot(2), snapshot(1)), RatingBook)
        return browsing.choose(casual(2, 2))
    }

    @Test
    fun positiveExperienceWithSomeLikelihoodChangesTheNextChoice() {
        assertEquals(2, choiceAfterVisit(RatingLikelihood.SOME, servedTick = 2))
        assertEquals(1, RatingBook.getById(2).positiveRatings)
    }

    @Test
    fun neutralExperienceWithSomeLikelihoodPreservesTheLowestIdTie() {
        assertEquals(1, choiceAfterVisit(RatingLikelihood.SOME, servedTick = 5))
        assertEquals(0, RatingBook.getById(2).positiveRatings)
        assertEquals(0, RatingBook.getById(2).negativeRatings)
    }

    @Test
    fun neutralExperienceWithAlwaysLikelihoodBreaksTheTie() {
        assertEquals(2, choiceAfterVisit(RatingLikelihood.ALWAYS, servedTick = 5))
        assertEquals(1, RatingBook.getById(2).positiveRatings)
    }
}
