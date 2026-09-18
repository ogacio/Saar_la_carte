package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**
 * describes a cook
 */
class Cook(
    private val type: CookType,
    private var id: Int? = null,
    private var busyUntil: Int = 0,
    private var batch: MutableList<Meal> = mutableListOf(),
    var clock: GlobalClock = GlobalClock
) {

    /**
     * receives a list of all the meals that have the same type, starts cooking them
     */
    fun startCooking(meals: MutableList<Meal>) {
        for (meal in meals) {
            meal.status = MealStatus.COOKING
        }
        busyUntil = clock.getTickInEvening() + meals[0].recipe.durationInTicks() - 1
        batch = meals
    }

    /**
     * checks if the meals are ready in this tick, if so -> cooked
     */
    fun cookingFinished(): MutableList<Meal>? {
        if (busyUntil <= clock.getTickInEvening()) {
            for (meal in batch) {
                meal.status = MealStatus.COOKED
            }
            busyUntil = 0
            val b = batch.toMutableList()
            batch.clear()
            return b
        } else {
            return null
        }
    }

    /**
     * returns if the cook is free or is currently cooking
     */
    fun isFree(): Boolean {
        return busyUntil == 0
    }

    /**
     * at the end of the evening resets the fields of the cook that change between nights
     */
    fun reset() {
        busyUntil = 0
        id = null
        batch.clear()
    }

    /**
     * sets the cook's ID
     */
    fun setId(id: Int?) { this.id = id }

    /**
     * returns the cook's ID
     */
    fun getId(): Int? = id

    /**
     * gets the cook's type
     */
    fun getType(): CookType = type
}
