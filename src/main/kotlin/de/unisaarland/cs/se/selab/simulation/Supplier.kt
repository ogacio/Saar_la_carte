package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

class Supplier (private var unavailableUntil: MutableMap<Ingredient, Int> = mutableMapOf<Ingredient, Int>(),
                private var clock:GlobalClock) {

    public fun setUnavailableUntil(unavailableUntil: MutableMap<Ingredient, Int>) {
        this.unavailableUntil = unavailableUntil
    }

    public fun resupply(p: Pantry, needed:Map<Ingredient, Int>) : Unit {

    }

    public fun markUnavailable(i: Ingredient,from: Int,duration: Int) : Unit {

    }

}