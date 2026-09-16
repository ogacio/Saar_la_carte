package de.unisaarland.cs.se.selab.sharedPackage

class Pantry (private var stock : MutableList<Pair<Ingredient, Int>> = mutableListOf(),  // (ingredient, amount)
              private var reserved : MutableList<Pair<Ingredient, Int>> = mutableListOf(),
) {

    fun restock(ingredient: Ingredient, amount: Int) {
        stock.add(Pair(ingredient, amount))
    }

    fun checkDateAndCleanOut() {
        stock.removeIf {(ingredient, _) -> !ingredient.reduceBestUntil()}
    }

    fun reserve(r:Recipe) : Boolean {
        if (!canCover(r)) return false
        val ingredients = r.ingredients
        val stockCopy = stock.toMutableList()
        for(i in ingredients) {
            reserved.add(Pair(i.ingredient, i.amount))
            // remove from stock
            var needed = i.amount
            for((index, entry) in stock.withIndex()) {
                val (ingredient, amount) = entry
                if (i.ingredient == ingredient) {
                    if(needed < amount) {
                        stockCopy[index] = Pair(ingredient, (amount - needed))
                        needed = 0
                    }
                    else {
                        needed -= amount
                        stockCopy[index] = Pair(ingredient, 0)
                    }
                }
                if (needed == 0) break
            }
        }
        stockCopy.removeIf{ (_, amount) -> amount == 0 }
        stock = stockCopy
        return true
    }

    fun deleteFromReserved(r:Recipe,cooked:Boolean) {
        if(cooked) {
            val ingredient = r.ingredients
            for (i in ingredient) {
                reserved.remove(Pair(i.ingredient, i.amount))
            }
        }
    }

    fun canCover(r:Recipe) : Boolean {
        val ingredients = r.ingredients
        for (i in ingredients) {
            var needed = i.amount
            for((ingredient,amount) in stock) {
                if (needed <= 0) break
                if (i.ingredient == ingredient) {
                    needed -= amount
                }
            }
            if(needed > 0) return false
        }
        return true
    }

    fun discardEvening() {
        reserved = mutableListOf()
    }
}