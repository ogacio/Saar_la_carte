package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.logging.Logger

class Statistics {
    private val cooked = mutableMapOf<Int, Int>()
    private val served = mutableMapOf<Int, Int>()
    private val delivered = mutableMapOf<Int, Int>()
    private val ratings = mutableMapOf<Int, Int>()
    fun recordCooked(restaurantId: Int, meals: Int): Unit{
        this.cooked[restaurantId] = (this.cooked[restaurantId]?:0)+meals
    }
    fun record(restaurantId: Int, customers: Int, delivered: Boolean): Unit{
        if (delivered){
            this.delivered[restaurantId] = (this.delivered[restaurantId]?:0)+customers
        }else{
            this.served[restaurantId] = (this.served[restaurantId]?:0)+customers
        }
    }
    fun recordRating(restaurantId: Int): Unit{
        this.ratings[restaurantId] = (this.ratings[restaurantId]?:0)+1
    }
    fun report(restaurantIds:List<Int>): Unit{
        Logger.statisticsCalculated()
        for (i in restaurantIds.sorted()){
            Logger.statMealsCooked(i,(cooked[i]?:0))
            Logger.statCustomersServed(i,(served[i]?:0))
            Logger.statCustomersDelivered(i,delivered[i]?:0)
            Logger.statRatings(i,ratings[i]?:0)
        }
    }
}