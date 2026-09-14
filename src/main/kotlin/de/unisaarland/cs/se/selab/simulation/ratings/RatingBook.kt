package de.unisaarland.cs.se.selab.simulation.ratings

class RatingBook {
    private val ratings: MutableMap<Int, RatingScore>

    public fun initializeRatings(restaurantId: Int, initialPositive: Int, initialNegative: Int) {
        ratings[restaurantId] = RatingScore().apply {
            setPositiveRatings(initialPositive)
            setNegativeRatings(initialNegative)
        }
    }

    public fun addRating(restaurantId: Int, rating: Rating) {
        ratings[restaurantId]?.addRating(rating)
    }

    public fun ratingScore(restaurantId: Int): Int? {
        return ratings[restaurantId]?.score()
    }
    fun getById(restaurantId: Int): RatingScore {
        return ratings[restaurantId]!!
    }
    
}