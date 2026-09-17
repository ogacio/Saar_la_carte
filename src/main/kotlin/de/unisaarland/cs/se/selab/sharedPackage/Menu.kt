package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.kitchen.Kitchen

/**
 * Manages the recipes of a restaurant and determines which dishes
 * are currently available for ordering based on ingredient
 * availability and the restaurant's current cooking capabilities.
 */
class Menu(
    private val recipes: MutableList<Recipe>,
    private val pantry: Pantry,
    private val kitchen: Kitchen
) {
    private val orderable = mutableListOf<Recipe>()

    /**
     * refreshes orderable recipes
     */
    fun refresh() {
        orderable.clear()
        for (i in recipes) {
            if (pantry.canCover(i) && kitchen.canCook(i)) {
                orderable.add(i)
            }
        }
    }

    /**
     * get available recipes
     */
    fun getOrderables(): List<Recipe> = orderable

    /**
     * get all recipes
     */
    fun getRecipes(): List<Recipe> = recipes
}
