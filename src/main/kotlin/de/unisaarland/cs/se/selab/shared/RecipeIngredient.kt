package de.unisaarland.cs.se.selab.shared

/**
 * The amount of one [Ingredient] a [Recipe] requires.
 *
 * Every recipe owns its own instances, so a RECIPE incident adapts each occurrence separately while
 * the referenced [Ingredient] stays shared.
 */
class RecipeIngredient(
    val ingredient: Ingredient,
    amount: Int,
) {
    /** The amount of [ingredient] the recipe currently requires. */
    var amount: Int = amount
        private set

    /**
     * Adapts the required amount by [percent], rounded down and never below one.
     */
    fun adaptBy(percent: Int) {
        val adapted = amount * (FULL_PERCENT + percent) / FULL_PERCENT
        amount = adapted.coerceAtLeast(1)
    }

    /** Percentage base used by the RECIPE incident. */
    private companion object {
        const val FULL_PERCENT = 100
    }
}
