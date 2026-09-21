package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.dishAssignment
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/** Manages the cooks mostly. */
class CookRoaster(
    val kitchenStaff: Map<CookType, Int>,
    val cooks: MutableList<Cook> = mutableListOf(),
    var nextId: Int = 1,
    var restaurantId: Int,
) {
    /** Called in parser, makes the cooks field. */
    fun initialiseCooks() {
        for (type in CookType.entries) {
            repeat(kitchenStaff[type] ?: 0) { cooks.add(Cook(type, clock = GlobalClock)) }
        }
    }

    /** The lowest-ranking eligible cook takes the dish, so the highest CookType ordinal. */
    fun startCooking(meals: MutableList<Meal>): Cook? {
        if (meals.isEmpty()) return null
        val recipe = meals[0].recipe
        val orderIds = meals.mapNotNull { it.orderId }.distinct().sorted()
        if (orderIds.isEmpty()) return null
        // sortedByDescending is stable, so among cooks of one type the first free one still takes it
        for (cook in cooks.sortedByDescending { it.getType().ordinal }) {
            if (!cook.isFree() || !recipe.cookableBy(cook.getType())) continue
            // "A cook is assigned an id the moment they start cooking the first assigned dish."
            if (cook.getId() == null) {
                cook.setId(nextId)
                nextId++
            }
            dishAssignment(
                restaurantId,
                checkNotNull(cook.getId()),
                cook.getType(),
                meals.size,
                recipe.getDishName(),
                orderIds[0],
                orderIds,
            )
            cook.startCooking(meals)
            return cook
        }
        return null
    }

    /** The meals finished this tick are reported by ascending cook id, empty pans skipped. */
    fun finished(): Map<Cook, List<Meal>> {
        val finished = linkedMapOf<Cook, List<Meal>>()
        for (cook in cooks.sortedBy { it.getId() ?: Int.MAX_VALUE }) {
            val batch = cook.cookingFinished()
            if (!batch.isNullOrEmpty()) {
                finished[cook] = batch
            }
        }
        return finished
    }

    /** ISSUE 2: how many cooks still have meals in the pan, for the status line. */
    fun cooksWithAFullPan(): Int = cooks.count { it.batchSize() > 0 }

    /** ISSUE 2: how many meals are still being cooked, for the status line. */
    fun mealsInPans(): Int = cooks.sumOf { it.batchSize() }

    /** Returns whether the restaurant employs a cook who could cook the recipe at all. */
    fun hasEligible(r: Recipe): Boolean = cooks.any { r.cookableBy(it.getType()) }

    /** Returns whether such a cook is also free this tick. */
    fun hasEligibleAndFree(r: Recipe): Boolean = cooks.any { it.isFree() && r.cookableBy(it.getType()) }

    /** Free cooks are fired first; a busy one hands its batch back to the queue. */
    fun changeStaff(type: CookType, delta: Int) {
        if (delta >= 0) {
            repeat(delta) { cooks.add(Cook(type, clock = GlobalClock)) }
            return
        }
        var toRemove = -delta
        val candidates = cooks.filter { it.getType() == type }.sortedBy { if (it.isFree()) 0 else 1 }
        for (cook in candidates) {
            if (toRemove == 0) break
            cook.releaseBatch()
            cooks.remove(cook)
            toRemove--
        }
    }

    /** Called at the end of the evening, resets every needed field for the next night. */
    fun resetEvening() {
        for (cook in cooks) {
            cook.reset()
        }
        nextId = 1
    }
}
