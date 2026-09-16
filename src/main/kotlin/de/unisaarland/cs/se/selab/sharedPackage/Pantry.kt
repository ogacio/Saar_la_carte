package de.unisaarland.cs.se.selab.sharedPackage

/**
 * pantry, stores the ingredients of the kitchen and updates every time somebody orders
 */
class Pantry(
    private var stock: MutableList<Pair<Ingredient, Int>> = mutableListOf(), // (ingredient, amount)
    private var reserved: MutableList<Pair<Ingredient, Int>> = mutableListOf(),
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
        stock.removeIf { (ingredient, _) -> !ingredient.reduceBestUntil() }
    }

    /**
     * we reserve the ingredients for a recipe (take the ingredients out of the stock)
    */
    fun reserve(r: Recipe): Boolean {
        if (!canCover(r)) return false
        val ingredients = r.ingredients
        val stockCopy = stock.toMutableList()
        for (i in ingredients) {
            reserved.add(Pair(i.ingredient, i.amount))
            // remove from stock
            var needed = i.amount
            for ((index, entry) in stock.withIndex()) {
                val (ingredient, amount) = entry
                if (i.ingredient == ingredient) {
                    if (needed < amount) {
                        stockCopy[index] = Pair(ingredient, (amount - needed))
                        needed = 0
                    } else {
                        needed -= amount
                        stockCopy[index] = Pair(ingredient, 0)
                    }
                }
                if (needed == 0) break
            }
        }
        stockCopy.removeIf { (_, amount) -> amount == 0 }
        stock = stockCopy
        return true
    }

    /**
     * we delete the cooked recipe from reserved
     */
    fun deleteFromReserved(r: Recipe, cooked: Boolean) {
        if (cooked) {
            val ingredient = r.ingredients
            for (i in ingredient) {
                reserved.remove(Pair(i.ingredient, i.amount))
            }
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
}
