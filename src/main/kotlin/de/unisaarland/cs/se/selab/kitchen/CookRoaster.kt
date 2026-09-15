package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.simulation.GlobalClock

class CookRoaster (
    val kitchenStaff : Map<CookType, Int>,
    val cooks : MutableList<Cook> = mutableListOf(),
    var nextId : Int = 1,
    val clock: GlobalClock
    )

{
    // has to be called early in restaurant, makes the cooks field
    fun initialiseCooks () {
        for((type,number) in kitchenStaff) {
            var remaining = number
            while(remaining != 0) {
                val cook = Cook(type, clock = clock)
                cooks.add(cook)
                remaining--
            }
        }
    }

    // returns the cook that starts cooking the meals - that all have the same type - (if any is free)
    fun startCooking(meals : MutableList<Meal>) : Cook? {
        val meal = meals[0]
        for(cook in cooks) {
            if(cook.isFree() && meal.recipe.cookTypes.contains(cook.getType())) {
                if(cook.getId() == null) {
                    cook.setId(nextId)
                    nextId++
                }
                cook.startCooking(meals)
                return cook
            }
        }
        return null
    }

    // returns all the meals that are cooked in this tick
    fun finished() : MutableList<Meal> {
        val finished = mutableListOf<Meal>()
        for(cook in cooks) {
            val finishedOrNot = cook.cookingFinished()
            if(finishedOrNot!= null) finished.addAll(finishedOrNot)
        }
        return finished
    }

    fun hasEligible(r: Recipe) : Boolean {
        for(cook in cooks) {
            if(cook.isFree() && r.cookTypes.contains(cook.getType())) return true
        }
        return false
    }

    fun changeStaff(type:CookType, delta: Int) {
        var delta = delta
        if(delta >= 0) {
            while(delta != 0) {
                val cook = Cook(type, clock = clock)
                cooks.add(cook)
                delta--
            }
        }
        else {
            val cookCopy = cooks.toMutableList()
            for(cook in cooks) {
                if(delta == 0) break
                if(cook.getType() == type) {
                    cookCopy.remove(cook)
                    delta++
                }
            }
            cooks.clear()
            cooks.addAll(cookCopy)
        }
    }

    fun resetEvening() {
        for (cook in cooks) {
            cook.reset()
        }
        nextId = 1
    }
}