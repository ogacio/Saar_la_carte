package de.unisaarland.cs.se.selab.simulation.ratings

/** Every restaurant's rating tally, keyed by restaurant id. */
object RatingBook {
    private val ratings: MutableMap<Int, RatingScore> = mutableMapOf()

    /** Starts tracking [restaurantId] with [initialPositive]/[initialNegative] seed ratings. */
    fun initializeRatings(restaurantId: Int, initialPositive: Int, initialNegative: Int) {
        ratings[restaurantId] = RatingScore(initialPositive, initialNegative)
    }

    /** Adds [rating] to [restaurantId]'s tally, if it has been initialized. */
    fun addRating(restaurantId: Int, rating: Rating) {
        ratings[restaurantId]?.addRating(rating)
    }

    /** The score of [restaurantId], or null if it has not been initialized. */
    fun ratingScore(restaurantId: Int): Int? {
        return ratings[restaurantId]?.score()
    }

    /** The rating tally of [restaurantId]; it must already be initialized. */
    fun getById(restaurantId: Int): RatingScore {
        return checkNotNull(ratings[restaurantId]) { "Restaurant $restaurantId has no rating tally" }
    }
}
