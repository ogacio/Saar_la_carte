package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe

/** The likes and dislikes of one subgroup of customers. */
class FoodPreference(
    private val size: Int,
    private val excluded: Set<Ingredient>,
    private val preferred: Set<Ingredient>,
    private val favouriteDishNames: List<String>,
) {
    /** The number of customers this preference applies to. */
    fun size(): Int = size

    /** The ingredients a customer with this preference will not eat. */
    fun excluded(): Set<Ingredient> = excluded

    /** The ingredients a customer with this preference prefers to eat. */
    fun preferred(): Set<Ingredient> = preferred

    /** The dish names a customer with this preference favours, in declared order. */
    fun favouriteDishNames(): List<String> = favouriteDishNames

    /** Whether a customer with this preference eats every ingredient of [recipe]. */
    fun accepts(recipe: Recipe): Boolean =
        recipe.ingredients.none { excluded.contains(it.ingredient) }

    /** How many of the preferred ingredients [recipe] contains, counted by kind not by amount. */
    fun rank(recipe: Recipe): Int =
        recipe.ingredients.count { preferred.contains(it.ingredient) }

    /** The first dish of [from] that is a favourite of this preference, or null. */
    fun firstFavourite(from: List<Recipe>): Recipe? =
        favouriteDishNames.firstNotNullOfOrNull { name -> from.firstOrNull { it.getDishName() == name } }
}
