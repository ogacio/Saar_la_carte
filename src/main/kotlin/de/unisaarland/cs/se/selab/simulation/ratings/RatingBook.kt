package de.unisaarland.cs.se.selab.simulation.ratings

object RatingBook {
    private val ratings: MutableMap<Int, RatingScore> = mutableMapOf()

    fun initializeRatings(restaurantId: Int, initialPositive: Int, initialNegative: Int) {
        ratings[restaurantId] = RatingScore(initialPositive, initialNegative)
    }

    fun addRating(restaurantId: Int, rating: Rating) {
        ratings[restaurantId]?.addRating(rating)
    }

    fun ratingScore(restaurantId: Int): Int? {
        return ratings[restaurantId]?.score()
    }

    fun getById(restaurantId: Int): RatingScore {
        return ratings[restaurantId]!!
    }

}