package de.unisaarland.cs.se.selab.simulation.ratings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * P05: [RatingBook] is a singleton without a reset, so every test uses restaurant ids of its own.
 * Covers the uninitialized paths that the integration tests never reach.
 */
class RatingBookUninitializedTest {

    @Test
    fun scoreOfAnUninitializedRestaurantIsNull() {
        assertNull(RatingBook.ratingScore(940))
    }

    @Test
    fun ratingAnUninitializedRestaurantIsIgnored() {
        RatingBook.addRating(941, Rating.POSITIVE)

        assertNull(RatingBook.ratingScore(941))
    }

    @Test
    fun initializedRestaurantAccumulatesRatingsIntoItsScore() {
        RatingBook.initializeRatings(942, initialPositive = 2, initialNegative = 1)

        RatingBook.addRating(942, Rating.POSITIVE)
        RatingBook.addRating(942, Rating.NEGATIVE)

        // seed 2-1 plus one positive and one negative -> 3 positive, 2 negative -> score 1
        assertEquals(1, RatingBook.ratingScore(942))
        assertEquals(1, RatingBook.getById(942).score())
    }

    @Test
    fun getByIdThrowsForARestaurantThatWasNeverInitialized() {
        assertFailsWith<IllegalStateException> { RatingBook.getById(943) }
    }
}
