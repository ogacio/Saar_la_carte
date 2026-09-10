package de.unisaarland.cs.se.selab
import de.unisaarland.cs.se.selab.data.RestaurantData
class BrowsingService (entries:MutableList<RestaurantData>, ratings: RatingBook){
    private var entries:MutableList<RestaurantData> = entries
    private val ratings:RatingBook = ratings
    fun refresh(snapshots: MutableList<RestaurantData>): Unit{
        entries = snapshots
    }
    fun choose(g: CustomerGroup): Int?{

    }
    fun chooseForEvent(g: CustomerGroup, eventEvening: Int): Int?{

    }
    private fun rank(candidates: MutableList<RestaurantData>): Int?{

    }

}