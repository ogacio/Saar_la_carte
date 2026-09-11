package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.simulation.GlobalClock

class Pantry (private var stock : MutableMap<Ingredient, MutableList<Int>>,
              private var reserved : Map<Ingredient, Int>,
              private var clock : GlobalClock
) {

    public fun checkDateAndCleanOut() : Unit {

    }

    public fun reserve(r:Recipe) : Boolean {
        return true
    }

    public fun deleteFromReserved(r:Recipe,cooked:Boolean) : Unit {

    }

    public fun canCover(r:Recipe) : Boolean {
        return true
    }

    public fun discardEvening() : Unit {

    }
}