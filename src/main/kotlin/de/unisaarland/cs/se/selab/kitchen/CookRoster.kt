package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.shared_data.Meal
import de.unisaarland.cs.se.selab.shared_data.Recipe

class CookRoaster (
    val kitchenStaff:Map<CookType, Int>,
    val cooks:MutableList<Cook>,
    var nextId: Int

) {
    public fun claim(r:Recipe,tick: Int): Cook? {

    }

    public fun finished(tick: Int):MutableList<Meal> {

    }

    public fun hasEligible(r: Recipe):Boolean {

    }

    public fun changeStaff(type:CookType,delta: Int):Unit {

    }

    public fun resetEvening():Unit {

    }
}