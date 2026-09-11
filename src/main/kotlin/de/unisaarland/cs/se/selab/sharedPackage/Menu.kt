package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.kicthen.Kitchen

class Menu(
    private val recipes: MutableList<Recipe>,
    private val pantry: Pantry,
    private val kitchen: Kitchen
) {
    private val orderable = mutableListOf<Recipe>()
    +orderable: MutableList<Recipe>

    +refresh(): Unit
}