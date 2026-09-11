package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup

class Order (
    var id : Int,
    var group : CustomerGroup,
    var restaurantId : Int,
    var customerGroupId : Int,
    var placedTick : Int,
    var isDelivery : Boolean,
    var meals : MutableList<Meal> )
{
    public fun dishCounts() : Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        for (meal in meals) {
            val dishName = meal.recipe.dishName
            if (out.containsKey(dishName)) {
                val put = out.getValue(dishName) + 1
                out.replace(dishName, out.getValue(dishName), put)
            }
            else out[dishName] = 1
        }
        return out
    }

    public fun allCooked() : Boolean {
        for (meal in meals) {
            if(meal.status != MealStatus.COOKED) return false
        }
        return true
    }

    public fun ticksSince(tick: Int) : Int {
        return tick - placedTick
    }
}