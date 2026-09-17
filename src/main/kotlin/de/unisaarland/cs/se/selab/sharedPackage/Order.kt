package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock

/**
 * this is how an order looks
 */
class Order(
    private var group: CustomerGroup,
    private var restaurantId: Int,
    private var customerGroupId: Int,
    private var placedTick: Int,
    private var isDelivery: Boolean,
    private var meals: MutableList<Meal>
) {
    /**
     * provides the next id
     */
    companion object {
        private var nextId = 1

        /**
         * when called, returns the current id, then increases
         */
        fun grantId(): Int = nextId++
    }
    val id = grantId()

    /**
     * counts how many dishes are in the order
     */
    fun dishCounts(): Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        for (meal in meals) {
            val dishName = meal.recipe.getDishName()
            if (out.containsKey(dishName)) {
                val put = out.getValue(dishName) + 1
                out.replace(dishName, out.getValue(dishName), put)
            } else {
                out[dishName] = 1
            }
        }
        return out
    }

    /**
     * returns if all the meals are cooked in the order or not
     */
    fun allCooked(): Boolean {
        for (meal in meals) {
            if (meal.status != MealStatus.COOKED) return false
        }
        return true
    }

    /**
     * returns the ticks that passed since the order was placed
     */
    fun ticksSince(): Int {
        return GlobalClock.getTickInEvening() - placedTick
    }

    /**
     * returns the customer group
     */
    fun getCustomerGroup(): CustomerGroup = group

    /**
     * returns the id
     */
    fun getId(): Int = id

    /**
     * returns meals
     */
    fun getMeals(): List<Meal> = meals
}
