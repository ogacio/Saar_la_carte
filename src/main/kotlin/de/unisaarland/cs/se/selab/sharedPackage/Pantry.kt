package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.pantryRemoved

/**
 * pantry, stores the ingredients of the kitchen and updates every time somebody orders
 */
class Pantry(
    private var stock: MutableList<Pair<Ingredient, Int>> = mutableListOf(), // (ingredient, amount)
    private var reserved: MutableList<Pair<Ingredient, Int>> = mutableListOf(),
    private val restaurantId: Int
) {

    /**
     * the supplier calls it, add the ordered ingredients to the stock
     */
    fun restock(ingredient: Ingredient, amount: Int) {
        stock.add(Pair(ingredient, amount))
    }

    /**
     * called in every preparation phase, we check the date of the ingredients,
     * if they expired, we remove / "throw them out" them from the stock
     */
    fun checkDateAndCleanOut() {
        stock.removeIf { (ingredient, amount) ->
            val expired = !ingredient.reduceBestUntil()
            if (expired) pantryRemoved(restaurantId, amount, ingredient.unit, ingredient.name)
            expired
        }
    }

    /**
     * we reserve the ingredients for a recipe (take the ingredients out of the stock)
     */
    fun reserve(r: Recipe): Boolean {
        if (!canCover(r)) return false
        val ingredients = r.ingredients
        for (i in ingredients) {
            reserved.add(Pair(i.ingredient, i.amount))
            removeFromStock(i.ingredient, i.amount)
        }

        return true
    }

    /**
     * removes the amount of ingredient from the stock to make reserve simpler
     */
    fun removeFromStock(i: Ingredient, amount: Int) {
        var needed = amount
        val stockCopy = stock.toMutableList()
        for ((index, entry) in stock.withIndex()) {
            val (ingredient, ingredientAmount) = entry
            if (i == ingredient) {
                if (needed < ingredientAmount) {
                    stockCopy[index] = Pair(ingredient, ingredientAmount - needed)
                    needed = 0
                } else {
                    needed -= ingredientAmount
                    stockCopy[index] = Pair(ingredient, 0)
                }
            }
            if (needed == 0) break
        }
        stockCopy.removeIf { (_, amount) -> amount == 0 }
        stock = stockCopy
    }

    /**
     * returns for how much ingredient we have in the pantry
     */
    fun getTotalIngredients(i: Ingredient): Int {
        val filtered = stock.filter { (ingredient, _) -> ingredient == i }
        return filtered.sumOf { (_, amount) -> amount }
    }

    /**
     * we delete the cooked recipe from reserved
     */
    fun deleteFromReserved(r: Recipe) {
        val ingredient = r.ingredients
        for (i in ingredient) {
            reserved.remove(Pair(i.ingredient, i.amount))
        }
    }

    /**
     * returns if we have enough ingredients to cover the recipe
     */
    fun canCover(r: Recipe): Boolean {
        val ingredients = r.ingredients
        for (i in ingredients) {
            var needed = i.amount
            for ((ingredient, amount) in stock) {
                if (needed <= 0) break
                if (i.ingredient == ingredient) {
                    needed -= amount
                }
            }
            if (needed > 0) return false
        }
        return true
    }

    /**
     * resets / "throws out" the reserved at the end of the night
     */
    fun discardEvening() {
        reserved = mutableListOf()
    }

    /**
     * returns the restaurantId
     */
    fun getRestaurantId() = restaurantId
}
