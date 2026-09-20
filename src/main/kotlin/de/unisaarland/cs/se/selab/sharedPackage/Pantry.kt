package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.pantryRemoved

/** Pantry, stores the ingredients of the kitchen and updates every time somebody orders. */
class Pantry(
    private val stock: MutableList<StockEntry> = mutableListOf(),
    private var reserved: MutableList<Pair<Ingredient, Int>> = mutableListOf(),
    private val restaurantId: Int,
) {
    /** Keeps the eleven Pantry(mutableListOf(rice to 500), id) test call sites compiling unchanged. */
    constructor(initial: MutableList<Pair<Ingredient, Int>>, restaurantId: Int) : this(
        initial.mapTo(mutableListOf()) { (ingredient, amount) ->
            StockEntry(ingredient, amount, ingredient.bestUntil)
        },
        mutableListOf(),
        restaurantId,
    )

    /** The supplier calls it; every delivery is its own entry and starts its own shelf life. */
    fun restock(ingredient: Ingredient, amount: Int) {
        if (amount > 0) {
            stock.add(StockEntry(ingredient, amount, ingredient.bestUntil))
        }
    }

    /** Every preparation phase: each delivery ages on its own counter, expired ones are thrown out. */
    // Open question: two deliveries expiring together give two Removed lines; the spec does not say.
    fun checkDateAndCleanOut() {
        val expired = stock.filter { !it.ageByOneEvening() }
        for (entry in expired) {
            pantryRemoved(restaurantId, entry.amount, entry.ingredient.unit, entry.ingredient.name)
        }
        stock.removeAll(expired)
    }

    /** Reserves the ingredients of a recipe, which takes them out of the stock. */
    fun reserve(r: Recipe): Boolean {
        if (!canCover(r)) return false
        for (i in r.ingredients) {
            reserved.add(Pair(i.ingredient, i.amount))
            removeFromStock(i.ingredient, i.amount)
        }
        return true
    }

    /** Takes [amount] of [i] out of the stock, emptying one package before opening the next. */
    fun removeFromStock(i: Ingredient, amount: Int) {
        var needed = amount
        for (entry in entriesFor(i)) {
            if (needed <= 0) break
            needed -= entry.take(needed)
        }
        stock.removeAll { it.isEmpty() }
    }

    /** Forum update: "open packages are given priority, followed by packages with the earliest best-before date". */
    private fun entriesFor(i: Ingredient): List<StockEntry> =
        stock.filter { it.ingredient == i }.sortedWith(compareBy({ !it.opened }, { it.bestUntil }))

    /** Returns how much of an ingredient the pantry holds, across all of its deliveries. */
    fun getTotalIngredients(i: Ingredient): Int =
        stock.filter { it.ingredient == i }.sumOf { it.amount }

    /** We delete the cooked recipe from reserved. */
    fun deleteFromReserved(r: Recipe) {
        for (i in r.ingredients) {
            reserved.remove(Pair(i.ingredient, i.amount))
        }
    }

    /** Returns whether the pantry can cover a whole recipe. */
    fun canCover(r: Recipe): Boolean {
        val needed = mutableMapOf<Ingredient, Int>()
        for (i in r.ingredients) {
            needed[i.ingredient] = (needed[i.ingredient] ?: 0) + i.amount
        }
        return needed.all { (ingredient, amount) -> getTotalIngredients(ingredient) >= amount }
    }

    /** Resets the reserved ingredients at the end of the night; the stock itself keeps. */
    fun discardEvening() {
        reserved = mutableListOf()
    }

    /** Returns the restaurantId. */
    fun getRestaurantId() = restaurantId
}
