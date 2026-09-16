package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

object Supplier {
    private var unavailableUntil: MutableMap<Ingredient, Pair<Int, Int>> = mutableMapOf<Ingredient, Pair<Int, Int>>()
    private var clock : GlobalClock = GlobalClock

    fun resupply(p: Pantry, needed:Map<Ingredient, Int>) {
        for((ingredient,amount) in needed) {
            val evening = clock.getEvening()
            // if the ingredient is available, we supply it
            if (!( unavailableUntil.containsKey(ingredient) && evening >= unavailableUntil[ingredient]!!.first
                && unavailableUntil[ingredient]!!.second <= evening) ) {
                p.restock(ingredient, amount)
            }
        }
    }

    fun markUnavailable(i: Ingredient,from: Int,duration: Int) {
        unavailableUntil[i] = Pair(from,duration)
    }

}