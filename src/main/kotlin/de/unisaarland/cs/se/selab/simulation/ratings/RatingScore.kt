package de.unisaarland.cs.se.selab.simulation.ratings


class RatingScore {
    private var positiveRatings: Int = 0
    private var negativeRatings: Int = 0

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