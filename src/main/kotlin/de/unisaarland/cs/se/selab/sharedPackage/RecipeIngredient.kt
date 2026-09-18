package de.unisaarland.cs.se.selab.sharedPackage
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

    init {
        ingredient.addUsage(this)
    }

    /**
     * Adapts the required amount by [percent], rounded down and never below one.
     */
    fun adaptBy(percent: Int) {
        val adapted = amount * (FULL_PERCENT + percent) / FULL_PERCENT
        amount = adapted.coerceAtLeast(1)
    }

    /**
     * The same value as [ingredient], for callers that use the getter style. The JVM name is changed
     * because the property's generated getter is already called `getIngredient`.
     */
    @JvmName("ingredientValue")
    fun getIngredient(): Ingredient = ingredient

    /** Percentage base used by the RECIPE incident. */
    private companion object {
        const val FULL_PERCENT = 100
    }
}
