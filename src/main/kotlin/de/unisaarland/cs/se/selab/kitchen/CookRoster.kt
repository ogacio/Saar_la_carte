package de.unisaarland.cs.se.selab.kicthen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Cook

class CookRoster (
    val kitchenStaff:Map<CookType, Int>,
    val cooks:MutableList<Cook>,
    var nextId: Int

) {
    public fun claim(r:Recipe,tick: Int):Cook? {

    }

    public fun finished(tick: Int):MutableList<Meal> {

    }

    public fun hasEligible(r: Recipe):Boolean {

    }

    public fun changeStaff(type:CookType,delta: Int):Unit {

    }

    public fun resetEvening():Unit {
        for (i in cooks){
            i.reset()
        }
        nextId = 1
    }
}