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
}
