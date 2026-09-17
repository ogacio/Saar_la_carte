package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.logging.Logger

/**
 * Collects simulation statistics for restaurants, including cooked
 * meals, served customers, delivered customers, and ratings, and
 * provides functionality for reporting the collected data.
 */
object Statistics {
    private val cooked = mutableMapOf<Int, Int>()
    private val served = mutableMapOf<Int, Int>()
    private val delivered = mutableMapOf<Int, Int>()
    private val ratings = mutableMapOf<Int, Int>()

    /**
     * add 1 meal to counter per restaurant
     */
    fun recordCooked(restaurantId: Int, meals: Int) {
        this.cooked[restaurantId] = (this.cooked[restaurantId] ?: 0) + meals
    }

    /**
     * add serve/delivery to counter per restaurant
     */
    fun record(restaurantId: Int, customers: Int, delivered: Boolean) {
        if (delivered) {
            this.delivered[restaurantId] = (this.delivered[restaurantId] ?: 0) + customers
        } else {
            this.served[restaurantId] = (this.served[restaurantId] ?: 0) + customers
        }
    }

    /**
     * add 1 rating to counter per restaurant
     */
    fun recordRating(restaurantId: Int) {
        this.ratings[restaurantId] = (this.ratings[restaurantId] ?: 0) + 1
    }

    /**
     * spit out to console when simulation ends
     */
    fun report(restaurantIds: List<Int>) {
        Logger.statisticsCalculated()
        for (i in restaurantIds.sorted()) {
            Logger.statMealsCooked(i, cooked[i] ?: 0)
            Logger.statCustomersServed(i, served[i] ?: 0)
            Logger.statCustomersDelivered(i, delivered[i] ?: 0)
            Logger.statRatings(i, ratings[i] ?: 0)
        }
    }
}
