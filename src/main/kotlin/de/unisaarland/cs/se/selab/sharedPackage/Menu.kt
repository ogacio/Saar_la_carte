package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.kitchen.Kitchen

class Menu(
    private val recipes: MutableList<Recipe>,
    private val pantry: Pantry,
    private val kitchen: Kitchen
) {
    private val orderable = mutableListOf<Recipe>()
    fun refresh(): Unit {
        orderable.clear()
        for (i in recipes) {
            if (pantry.canCover(i)&&kitchen.canCook(i)){
                orderable.add(i)
            }
        }
    }
    fun getOrderables():List<Recipe> = orderable
}