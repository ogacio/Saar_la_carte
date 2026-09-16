package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock

class Cook (
    private val type: CookType,
    private var id: Int? = null,
    private var busyUntil: Int = 0,
    private var batch: MutableList<Meal> = mutableListOf(),
    var clock : GlobalClock = GlobalClock
)

{

    // receives a list of all the meals that have the same type, starts cooking them
    fun startCooking(meals : MutableList<Meal>) {
        for(meal in meals) {
            meal.status = MealStatus.COOKING
        }
        busyUntil = clock.getCurrentTick() + meals[0].recipe.durationInTicks()
        batch = meals
    }

    // checks if the meals are ready in this tick, if so -> cooked
    fun cookingFinished() : MutableList<Meal>? {
        if(busyUntil <= clock.getCurrentTick()) {
            for(meal in batch) {
                meal.status = MealStatus.COOKED
            }
            busyUntil = 0
            val b = batch
            batch.clear()
            return b
        }
        else return null
    }

    fun isFree() : Boolean {
        return busyUntil == 0
    }

    fun reset() {
        busyUntil = 0
        id = null
        batch.clear()
    }

    fun setId(id:Int?) { this.id = id }
    fun getId() : Int? = id
    fun getType() : CookType = type
}