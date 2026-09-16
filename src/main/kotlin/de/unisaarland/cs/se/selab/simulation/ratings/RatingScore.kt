package de.unisaarland.cs.se.selab.simulation.ratings


class RatingScore(initialPositive: Int, initialNegative: Int) {
    var positiveRatings: Int = initialPositive
    var negativeRatings: Int = initialNegative

    public fun addRating(rating: Rating) {
        when (rating) {
            Rating.POSITIVE -> positiveRatings++
            Rating.NEGATIVE -> negativeRatings++
        }
    }

    public fun score(): Int {
        return positiveRatings - negativeRatings
    }

}