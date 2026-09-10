package de.unisaarland.cs.se.selab.shared

/**
 * One kind of ingredient the supplier sells and the restaurants cook with.
 *
 * The packaging volume is mutable because a PACKAGING incident changes it globally; the object is
 * shared by every recipe that uses the ingredient, so the change reaches all of them at once.
 */
class Ingredient(
    val name: String,
    val unit: UnitType,
    packagingVolume: Int,
    val bestUntil: Int,
) {
    /** The base amount in which the ingredient can be obtained from the supplier. */
    var packagingVolume: Int = packagingVolume
        private set

    /**
     * The number of whole packages needed to cover [amount] of this ingredient.
     */
    fun packagesFor(amount: Int): Int {
        if (amount <= 0) {
            return 0
        }
        return (amount + packagingVolume - 1) / packagingVolume
    }

    /**
     * Applies a PACKAGING incident by setting the packaging volume to [newVolume].
     */
    fun changePackaging(newVolume: Int) {
        if (newVolume > 0) {
            packagingVolume = newVolume
        }
    }
}

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

    /** Length of a tick, used to convert the recipe duration into ticks. */
    private companion object {
        const val MINUTES_PER_TICK = 10
    }
}
