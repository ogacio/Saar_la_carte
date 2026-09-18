package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.dishAssignment
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**
 * manages the cooks mostly
 */
class CookRoaster(
    val kitchenStaff: Map<CookType, Int>,
    val cooks: MutableList<Cook> = mutableListOf(),
    var nextId: Int = 1,
    var restaurantId: Int
) {
    /**
     * called in parser, makes the cooks field
     */
    fun initialiseCooks() {
        for (type in CookType.entries) {
            var remaining = kitchenStaff[type] ?: 0
            while (remaining != 0) {
                val cook = Cook(type, clock = GlobalClock)
                cooks.add(cook)
                remaining--
            }
        }
    }

    /**
     * returns the cook that starts cooking the meals - that all have the same type - (if any is free)
     */
    fun startCooking(meals: MutableList<Meal>): Cook? {
        val meal = meals[0]
        for (cook in cooks) {
            if (cook.isFree() && meal.recipe.cookTypes.contains(cook.getType())) {
                if (cook.getId() == null) {
                    cook.setId(nextId)
                    nextId++
                }
                val cookId = cook.getId()
                if (cookId != null) {
                    val m = meals[0]
                    dishAssignment(
                        restaurantId,
                        cookId,
                        cook.getType(),
                        meals.size,
                        m.recipe.getDishName(),
                        m.orderId,
                        meals.map { it.orderId }.toSet()
                    )
                    cook.startCooking(meals)
                    return cook
                }
            }
        }
        return null
    }

    /**
     * returns all the meals that are cooked in this tick
     */
    fun finished(): MutableMap<Cook, MutableList<Meal>> {
        val finished = mutableMapOf<Cook, MutableList<Meal>>()
        for (cook in cooks) {
            val finishedOrNot = cook.cookingFinished()
            if (finishedOrNot != null) finished[cook] = finishedOrNot
        }
        return finished
    }

    /**
     * returns if we have a cook to cook the recipe
     */
    fun hasEligible(r: Recipe): Boolean {
        for (cook in cooks) {
            if (r.cookTypes.contains(cook.getType())) return true
        }
        return false
    }

    /**
     * returns if we have a cook who is free to cook the recipe
     */
    fun hasEligibleAndFree(r: Recipe): Boolean {
        for (cook in cooks) {
            if (cook.isFree() && r.cookTypes.contains(cook.getType())) return true
        }
        return false
    }

    /**
     * triggered by incident, adds or removes cooks
     */
    fun changeStaff(type: CookType, delta: Int) {
        var d = delta
        if (d >= 0) {
            while (d != 0) {
                val cook = Cook(type, clock = GlobalClock)
                cooks.add(cook)
                d--
            }
        } else {
            val cookCopy = cooks.toMutableList()
            for (cook in cooks) {
                if (d == 0) break
                if (cook.getType() == type) {
                    cookCopy.remove(cook)
                    d++
                }
            }
            cooks.clear()
            cooks.addAll(cookCopy)
        }
    }

    /**
     * called at the end of the evening, resets every needed field for the next night
     */
    fun resetEvening() {
        for (cook in cooks) {
            cook.reset()
        }
        nextId = 1
    }
}
