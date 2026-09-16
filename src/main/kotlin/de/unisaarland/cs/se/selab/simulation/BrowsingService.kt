package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook


class BrowsingService(var entries: MutableList<RestaurantData>, var ratings: RatingBook) {
    fun refresh(snapshots: MutableList<RestaurantData>) {
        entries = snapshots
    }
    fun choose(g: CustomerGroup): Int? {
        var candidates: MutableList<RestaurantData> = entries
        candidates = candidates.filter {
            it.getType() in g.restaurantTypes()
        }.filter {
            it.openAt(GlobalClock.getTickInEvening())
        }.toMutableList()
        candidates = candidates.filter {
                c ->
            g.preferences().all { preference ->
                c.getDishes().any {
                    it.getIngredients().none { it.getIngredient() in preference.excluded() }
                }
            }
        }.toMutableList()
        if (g.isDelivery()) {
            candidates = candidates.filter { it.getFreeDrivers() > 0 }.toMutableList()
        } else {
            candidates = candidates.filter {
                (it.getFreeSeats()[g.tableType()] ?: 0) >= g.groupSize()
            }.toMutableList()
        }
        val id = rank(candidates)
        if (id != null) {
            candidates.first { it.getId() == id }.take(g)
        }
        return id
    }
    fun chooseForEvent(g: CustomerGroup, eventEvening: Int): Int? {
        var candidates: MutableList<RestaurantData> = entries
        candidates = candidates.filter {
            it.getType() in g.restaurantTypes()
        }.filter { it.openAt(g.visitingTick()) }.filter {
            it.getHostsEvents()
        }.toMutableList()
        candidates = candidates.filter { c ->
            g.preferences().all { preference ->
                c.getDishes().any {
                    it.getIngredients().none {
                        it.getIngredient() in preference.excluded()
                    }
                }
            }
        }.toMutableList()
        candidates = candidates.filter { it.eventSeatsLeft(g.getEventEvening()) >= g.groupSize() }.toMutableList()
        val id = rank(candidates)
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
            return candidates.get(0).getId()
        }
    }
}
