package de.unisaarland.cs.se.selab
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData

class BrowsingService (entries:MutableList<RestaurantData>, ratings: RatingBook){
    private var entries:MutableList<RestaurantData> = entries
    private val ratings:RatingBook = ratings
    fun refresh(snapshots: MutableList<RestaurantData>): Unit{
        entries = snapshots
    }
    fun choose(g: CustomerGroup): Int?{
        var candidates:MutableList<RestaurantData> = entries
        candidates = candidates.filter{it.getType() in g.getRestaurantTypes()}.filter{it.openAt(GlobalClock.getTickInEvening())}.toMutableList()
        candidates = candidates.filter{c -> g.getPreferences().all{preference -> c.getDishes().any{it.getIngredients().none{it.getIngredient() in preference.getExcluded()}}}}.toMutableList()
        if (g.isDelivery()) {
            candidates = candidates.filter{it.getFreeDrivers()>0}.toMutableList()
        }else {
            candidates = candidates.filter {
                (it.getFreeSeats()[g.getTableType()] ?: 0) >= g.getGroupSize()
            }.toMutableList()
        }
        val id = rank(candidates)
        if (id!=null){
               candidates.first{it.getId() == id}.take(g)
        }
        return id
    }
    fun chooseForEvent(g: CustomerGroup, eventEvening: Int): Int?{
        var candidates:MutableList<RestaurantData> = entries
        candidates = candidates.filter{it.getType() in g.getRestaurantTypes()}.filter{it.openAt(g.getVisitingTick())}.filter{it.getHostsEvents()}.toMutableList()
        candidates = candidates.filter{c -> g.getPreferences().all{preference -> c.getDishes().any{it.getIngredients().none{it.getIngredient() in preference.getExcluded()}}}}.toMutableList()
        candidates = candidates.filter{it.eventSeatsLeft(g.getEventEvening())>=g.getGroupSize()}.toMutableList()
        val id = rank(candidates)
        if (id!=null){
            candidates.first{it.getId() == id}.takeForEvent(g, eventEvening)
        }
        return id
    }
    private fun rank(candidates: MutableList<RestaurantData>): Int?{
        if (candidates.isEmpty()){
            return null
        }else{
            var highest = ratings.getById(candidates.get(0).getId()).score()
            for (i in candidates){
                if (ratings.getById(i.getId()).score() > highest) {
                    highest = ratings.getById(i.getId()).score()
                }
            }
            candidates.removeIf {
                ratings.getById(it.getId()).score() < highest
            }
            candidates.sortWith(compareBy { it.getId() })
            return candidates.get(0).getId()

        }
    }
}