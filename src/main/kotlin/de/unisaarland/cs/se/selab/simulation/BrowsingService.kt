package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook

/**
 * Provides the information required for customer groups to browse
 * available restaurants, evaluate whether a restaurant is suitable,
 * and select a restaurant based on current restaurant data and ratings.
 */
class BrowsingService(var entries: MutableList<RestaurantData>, var ratings: RatingBook) {

    /**
     * refreshes RestaurantData
     */
    fun refresh(snapshots: MutableList<RestaurantData>) {
        entries = snapshots
    }

    /**
     * choose Restaurant--casual
     */
    fun choose(g: CustomerGroup): Int? {
        var candidates: List<RestaurantData> = entries
        candidates = candidates.filter {
            it.getType() in g.restaurantTypes()
        }.filter {
            it.openAt(GlobalClock.getTickInEvening())
        }
        candidates = candidates.filter {
                c ->
            g.preferences().all { preference ->
                c.getDishes().any {
                    it.getIngredients().none { it.getIngredient() in preference.excluded() }
                }
            }
        }
        if (g.isDelivery()) {
            candidates = candidates.filter { it.getFreeDrivers() > 0 }
        } else {
            candidates = candidates.filter {
                (it.getFreeSeats()[g.tableType()] ?: 0) >= g.groupSize()
            }
        }
        val id = rank(candidates.toMutableList())
        if (id != null) {
            candidates.first { it.getId() == id }.take(g)
        }
        return id
    }

    /**
     * choose restaurant--event
     */
    fun chooseForEvent(g: CustomerGroup, eventEvening: Int): Int? {
        var candidates: List<RestaurantData> = entries
        candidates = candidates.filter {
            it.getType() in g.restaurantTypes()
        }.filter { it.openAt(g.visitingTick()) }.filter {
            it.getHostsEvents()
        }
        candidates = candidates.filter { c ->
            g.preferences().all { preference ->
                c.getDishes().any {
                    it.getIngredients().none {
                        it.getIngredient() in preference.excluded()
                    }
                }
            }
        }
        candidates = candidates.filter { it.eventSeatsLeft(eventEvening) >= g.groupSize() }
        val id = rank(candidates.toMutableList())
        if (id != null) {
            candidates.first { it.getId() == id }.takeForEvent(g)
        }
        return id
    }
    private fun rank(candidates: MutableList<RestaurantData>): Int? {
        if (candidates.isEmpty()) {
            return null
        } else {
            var highest = ratings.getById(candidates.get(0).getId()).score()
            for (i in candidates) {
                if (ratings.getById(i.getId()).score() > highest) {
                    highest = ratings.getById(i.getId()).score()
                }
            }
            candidates.removeIf {
                ratings.getById(it.getId()).score() < highest
            }
            candidates.sortWith(compareBy { it.getId() })
            return candidates[0].getId()
        }
    }
}
