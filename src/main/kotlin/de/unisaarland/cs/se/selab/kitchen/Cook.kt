package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal

class Cook (
    private val type : CookType,
    private var id : Int? = null,
    private var busyUntil : Int? = null,
    private var batch : MutableList<Meal>,
    )

{
    // recieves a list of all the meals that have the same type
    fun startCooking(meals:MutableList<Meal>,tick: Int):Unit {
        /* var meal in meals
        busyUntil = tick + meal.getRecipe().durationInTicks() */
    }

    fun endCooking(tick: Int) : MutableList<Meal>? {
        if(busyUntil == tick) {
            busyUntil = null
            return batch
        }
        else return null
    }

    fun isFree(tick: Int):Boolean {
        return busyUntil != null
    }
    fun reset(): Unit{
        id = null
        busyUntil = null
        batch.clear()
    }
}