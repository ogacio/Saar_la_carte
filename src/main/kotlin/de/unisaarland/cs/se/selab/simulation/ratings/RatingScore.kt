package de.unisaarland.cs.se.selab.simulation.ratings


class RatingScore {
    var positiveRatings: Int = 0
    var negativeRatings: Int = 0

    public fun addRating(rating: Rating) {
        when (rating) {
            Rating.POSITIVE -> positiveRatings++
            Rating.NEGATIVE -> negativeRatings++
        }
    }

    public fun score: Int {
        return positiveRatings - negativeRatings
    }

}