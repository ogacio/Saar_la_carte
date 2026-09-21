package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/** Describes a cook. */
class Cook(
    private val type: CookType,
    private var id: Int? = null,
    private var busyUntil: Int = 0,
    private var batch: MutableList<Meal> = mutableListOf(),
    var clock: GlobalClock = GlobalClock,
) {
    /** Starts a batch of one recipe; ceil(minutes / 10) - 1 ticks of waiting, so 10 minutes is none. */
    fun startCooking(meals: MutableList<Meal>) {
        for (meal in meals) {
            meal.status = MealStatus.COOKING
        }
        busyUntil = clock.getTickInEvening() + meals[0].recipe.durationInTicks() - 1
        batch = meals
    }

    /** Hands the batch over if it is done this tick; an idle cook returns null, not an empty list. */
    fun cookingFinished(): MutableList<Meal>? {
        if (batch.isEmpty() || busyUntil > clock.getTickInEvening()) return null
        for (meal in batch) {
            if (meal.status == MealStatus.COOKING) meal.status = MealStatus.COOKED
        }
        val done = batch.toMutableList()
        batch.clear()
        return done
    }

    /** ISSUE 6: gives the meals back to the queue, used when this cook leaves mid-batch. */
    fun releaseBatch() {
        for (meal in batch) {
            if (meal.status == MealStatus.COOKING) meal.status = MealStatus.QUEUED
        }
        batch.clear()
        busyUntil = 0
    }

    /** ISSUE 2: how many meals this cook has in the pan right now. */
    fun batchSize(): Int = batch.size

    /** A cook is free again in the tick after the one its batch finished in. */
    fun isFree(): Boolean = busyUntil < clock.getTickInEvening()

    /** At the end of the evening resets the fields of the cook that change between nights. */
    fun reset() {
        busyUntil = 0
        id = null
        batch.clear()
    }

    /** Sets the cook's ID. */
    fun setId(id: Int?) {
        this.id = id
    }

    /** Returns the cook's ID. */
    fun getId(): Int? = id

    /** Gets the cook's type. */
    fun getType(): CookType = type
}
