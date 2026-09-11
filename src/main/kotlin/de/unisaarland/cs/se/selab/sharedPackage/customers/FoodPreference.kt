package de.unisaarland.cs.se.selab.sharedPackage

/** The likes and dislikes of one subgroup of customers. */
class FoodPreference(
    val size: Int,
    val excluded: Set<Ingredient>,
    val preferred: Set<Ingredient>,
    val favouriteDishNames: List<String>,
) {
    /** Whether a customer with this preference eats every ingredient of [recipe]. */
    fun accepts(recipe: Recipe): Boolean =
        recipe.ingredients.none { excluded.contains(it.ingredient) }

    /** How many of the preferred ingredients [recipe] contains, counted by kind not by amount. */
    fun rank(recipe: Recipe): Int =
        recipe.ingredients.count { preferred.contains(it.ingredient) }

    /** The first dish of [from] that is a favourite of this preference, or null. */
    fun firstFavourite(from: List<Recipe>): Recipe? =
        favouriteDishNames.firstNotNullOfOrNull { name -> from.firstOrNull { it.dishName == name } }
}
