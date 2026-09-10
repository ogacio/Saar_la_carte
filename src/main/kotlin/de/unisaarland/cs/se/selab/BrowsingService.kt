package de.unisaarland.cs.se.selab
import de.unisaarland.cs.se.selab.data.RestaurantData
import de.unisaarland.cs.se.selab.data.RestaurantType

class BrowsingService (entries:MutableList<RestaurantData>, ratings: RatingBook){
    private var entries:MutableList<RestaurantData> = entries
    private val ratings:RatingBook = ratings
    fun refresh(snapshots: MutableList<RestaurantData>): Unit{
        entries = snapshots
    }
    fun choose(g: CustomerGroup): Int?{
        val candidates = entries
        candidates.filter{it.type in g.restaurantTypes}.filter{it.openAt(GlobalClock.getTickInEvening())}
        candidates.filter{c -> g.getPreferences.all{preference -> c.getDishes().any{it.getIngredients().none{it.getIngredient() in preference.getExcluded()}}}}
        if (g.isDelivery()) {
            candidates.filter{it.getFreeDrivers()>0}
        }else {
            candidates.filter{it.getFreeSeats() >= g.getGroupSize()}
        }
        val id = rank(candidates)
    }
    fun chooseForEvent(g: CustomerGroup, eventEvening: Int): Int?{
        var candidates = entries
        candidates.filter{it.type in g.restaurantTypes}.filter{it.openAt(g.visitingTick)}
        candidates.filter{c -> g.getPreferences.all{preference -> c.getDishes().any{it.getIngredients().none{it.getIngredient() in preference.getExcluded()}}}}
        candidates.filter{it.eventSeatsLeft(g.getEventEvening())>=g.getGroupSize()}
        val id = rank(candidates)
    }
    private fun rank(candidates: MutableList<RestaurantData>): Int?{
        if (candidates.isEmpty()){
            return null
        }else{
            var highest = ratings.getById(candidates.get(0).id).score()
            for (i in candidates){
                if (ratings.getById(i.id).score() > highest) {
                    highest = ratings.getById(i.id).score()
                }
            }
            for (i in candidates){
                if (ratings.getById(i.id).score() < highest){
                    candidates.remove(i)
                }
            }
            candidates.sortWith(compareBy { it.id })
            return candidates.get(0).id

        }
    }

}