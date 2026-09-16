package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.kitchen.CookType

/**
 * The recipe for one dish: what it needs, how long it takes and who may cook it.
 */
class Recipe(
    val id: Int,
    val dishName: String,
    val minuteDuration: Int,
    val cookTypes: Set<CookType>,
    val ingredients: MutableList<RecipeIngredient>,
    val basicDishFor: RestaurantType?,
) {
    /**
     * The cooking duration in whole ticks; a tick covers ten minutes, partial ticks are rounded up.
     */
    fun durationInTicks(): Int = (minuteDuration + MINUTES_PER_TICK - 1) / MINUTES_PER_TICK

    /**
     * Whether this recipe is the default recipe of a basic dish of restaurant type [type].
     */
    fun isBasicFor(type: RestaurantType): Boolean = basicDishFor == type

    /**
     * Whether a cook of type [type] is able to cook this dish.
     */
    fun cookableBy(type: CookType): Boolean = cookTypes.contains(type)

    /**
     * The same list as [ingredients], for callers that use the getter style. The JVM name is changed
     * because the property's generated getter is already called `getIngredients`.
     */
    @JvmName("ingredientList")
    fun getIngredients(): List<RecipeIngredient> = ingredients

    /** Length of a tick, used to convert the recipe duration into ticks. */
    private companion object {
        const val MINUTES_PER_TICK = 10
    }
}
