package de.unisaarland.cs.se.selab.sharedPackage
/** One delivery of [ingredient] in one pantry, with its own remaining amount and shelf life. */
class StockEntry(
    val ingredient: Ingredient,
    amount: Int,
    bestUntil: Int,
) {
    /** How much of this delivery is left. */
    var amount: Int = amount
        private set

    /** Evenings this delivery still lasts, counted down once per preparation phase. */
    var bestUntil: Int = bestUntil
        private set

    /** Whether anything has been taken from this package yet; open packages are used up first. */
    var opened: Boolean = false
        private set

    /** Takes up to [wanted] from this delivery and returns how much it could actually give. */
    fun take(wanted: Int): Int {
        if (wanted <= 0 || amount <= 0) return 0
        val taken = minOf(wanted, amount)
        amount -= taken
        opened = true
        return taken
    }

    /** Ages the delivery by one evening and returns whether it is still good afterwards. */
    fun ageByOneEvening(): Boolean {
        bestUntil--
        return bestUntil > 0
    }

    /** Whether nothing is left of this delivery, so the pantry can drop the entry. */
    fun isEmpty(): Boolean = amount <= 0
}
