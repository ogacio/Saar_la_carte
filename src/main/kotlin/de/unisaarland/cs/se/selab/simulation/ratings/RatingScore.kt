package de.unisaarland.cs.se.selab.simulation.ratings

/** The positive/negative rating tally of one restaurant. */
class RatingScore(initialPositive: Int, initialNegative: Int) {
    var positiveRatings: Int = initialPositive
    var negativeRatings: Int = initialNegative

    /** Adds [rating] to the tally. */
    fun addRating(rating: Rating) {
        when (rating) {
            Rating.POSITIVE -> positiveRatings++
            Rating.NEGATIVE -> negativeRatings++
        }
    }

    /** The positive ratings minus the negative ratings. */
    fun score(): Int {
        return positiveRatings - negativeRatings
    }
}
