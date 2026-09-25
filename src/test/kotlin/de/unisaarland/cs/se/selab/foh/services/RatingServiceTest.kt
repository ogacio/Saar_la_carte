package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.RESTAURANT_ID
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import de.unisaarland.cs.se.selab.testsupport.Fixtures.regular
import de.unisaarland.cs.se.selab.testsupport.Fixtures.subUnits
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** P05: rating after a visit and after a failed reservation. */
class RatingServiceTest {

    private val sbu = subUnits()
    private val service = RatingService()

    /** The ratings of the test restaurant as (positive, negative). */
    private fun ratings(): Pair<Int, Int> =
        RatingBook.getById(RESTAURANT_ID).let { it.positiveRatings to it.negativeRatings }

    @BeforeTest
    fun freshRatings() {
        RatingBook.initializeRatings(RESTAURANT_ID, 0, 0)
    }

    /** A visit that ordered at tick 1 and whose last meal was served at tick [lastServedTick]. */
    private fun servedVisit(group: CustomerGroup, lastServedTick: Int): Visit {
        val visit = Visit(group)
        visit.orderedTick = 1
        group.members().forEach { it.receive(Meal(1, it, recipe(1)), lastServedTick) }
        return visit
    }

    @Test
    fun failedReservationIsANegativeRatingAndAFailedAttempt() {
        val group = regular(3, 2)
        val log = captureLog()

        service.rateFailedReservation(group, sbu)
        service.logStatus(sbu)

        assertEquals(0 to 1, ratings())
        assertEquals(1, group.failedAttempts())
        assertEquals(
            listOf(
                "[INFO] Rating (R 1): Group 3 rates the restaurant 1 with NEGATIVE rating, " +
                    "leading to 0 positive ratings and 1 negative ratings.",
                "[DEBUG] Rating Status (R 1): 1 groups performed ratings this tick.",
            ),
            logLines(log),
        )
    }

    @Test
    fun servedOnTimeIsPositiveAndResetsTheFailedAttempts() {
        val group = regular(3, 2).also { it.recordFailedAttempt() }
        val visit = servedVisit(group, lastServedTick = 3)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(1 to 0, ratings())
        assertEquals(0, group.failedAttempts())
        assertTrue(visit.rated)
    }

    @Test
    fun failedVisitCountsAsFailedAttemptForRegulars() {
        val group = regular(3, 2)
        val visit = Visit(group).also { it.failedAttempt = true }
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 1, ratings())
        assertEquals(1, group.failedAttempts())
    }

    @Test
    fun casualGroupThatNeverRatesLeavesNoRating() {
        val visit = servedVisit(casual(3, 2, likelihood = RatingLikelihood.NEVER), lastServedTick = 2)
        val log = captureLog()

        service.rate(visit, sbu)
        service.logStatus(sbu)

        assertEquals(0 to 0, ratings())
        assertEquals(listOf("[DEBUG] Rating Status (R 1): 0 groups performed ratings this tick."), logLines(log))
    }

    @Test
    fun casualGroupWithSomeLikelihoodSkipsNeutralExperiences() {
        val visit = servedVisit(casual(3, 2, likelihood = RatingLikelihood.SOME), lastServedTick = 5)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 0, ratings())
    }

    @Test
    fun casualGroupWithSomeLikelihoodRatesNegativeExperiences() {
        val visit = servedVisit(casual(3, 2, likelihood = RatingLikelihood.SOME), lastServedTick = 6)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 1, ratings())
    }

    @Test
    fun casualGroupWithAlwaysLikelihoodRatesNeutralAsPositive() {
        val visit = servedVisit(casual(3, 2, likelihood = RatingLikelihood.ALWAYS), lastServedTick = 5)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(1 to 0, ratings())
    }

    @Test
    fun eventGroupAlwaysRatesAFailedReservation() {
        captureLog()

        service.rateFailedReservation(event(3, 4), sbu)

        assertEquals(0 to 1, ratings())
    }

    @Test
    fun statusStartsAfreshAfterLogging() {
        service.rateFailedReservation(regular(3, 2), sbu)
        service.logStatus(sbu)
        val log = captureLog()

        service.logStatus(sbu)

        assertEquals(listOf("[DEBUG] Rating Status (R 1): 0 groups performed ratings this tick."), logLines(log))
    }

    /** Event group rates positively when served well within the 4-tick expectation window. */
    @Test
    fun eventGroupRatesPositiveWhenServedPromptly() {
        val visit = servedVisit(event(3, 4), lastServedTick = 2)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(1 to 0, ratings())
    }

    /** Event group rates negatively when the food arrives too late, same as a regular group would. */
    @Test
    fun eventGroupRatesNegativeWhenServedTooLate() {
        val visit = servedVisit(event(3, 4), lastServedTick = 6)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 1, ratings())
    }

    /** logStatus counts every group that rated this tick, not just the most recent one. */
    @Test
    fun logStatusCountsAllGroupsThatRatedThisTick() {
        val first = servedVisit(regular(3, 2), lastServedTick = 3)
        val second = servedVisit(regular(5, 2), lastServedTick = 3)
        val log = captureLog()

        service.rate(first, sbu)
        service.rate(second, sbu)
        service.logStatus(sbu)

        assertEquals(
            "[DEBUG] Rating Status (R 1): 2 groups performed ratings this tick.",
            logLines(log).last(),
        )
    }

    /** A neutral experience for a NEVER-likelihood casual group still produces no rating. */
    @Test
    fun casualGroupWithNeverLikelihoodSkipsEvenAPositiveExperience() {
        val visit = servedVisit(casual(3, 2, likelihood = RatingLikelihood.NEVER), lastServedTick = 2)
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 0, ratings())
    }

    /** A failed visit for an event group counts as a failed attempt the same way it does for regulars. */
    @Test
    fun failedVisitCountsAsFailedAttemptForEvents() {
        val group = event(3, 4)
        val visit = Visit(group).also { it.failedAttempt = true }
        captureLog()

        service.rate(visit, sbu)

        assertEquals(0 to 1, ratings())
    }
}
