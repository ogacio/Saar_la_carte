package de.unisaarland.cs.se.selab.simulation
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.procured
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.restocked
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

/**
 * provides ingredients to the pantry
 */
object Supplier {
    private val unavailableUntil: MutableMap<Ingredient, Pair<Int, Int>> = mutableMapOf()

    /**
     * adds to pantry the ingredients in the needed quantities
     */
    fun resupply(p: Pantry, needed: Map<Ingredient, Int>) {
        needed.entries.sortedBy { it.key.name }
        for ((ingredient, amount) in needed) {
            val evening = GlobalClock.getEvening()
            // if the ingredient is available, we supply it
            if (!(
                    unavailableUntil.containsKey(ingredient) &&
                        evening >= checkNotNull(unavailableUntil[ingredient]).first &&
                        evening <= checkNotNull(unavailableUntil[ingredient]).second
                    )
            ) {
                p.restock(ingredient, ingredient.packagesFor(amount) * ingredient.packagingVolume)
                procured(
                    p.getRestaurantId(),
                    ingredient.packagesFor(amount) * ingredient.packagingVolume,
                    ingredient.unit,
                    ingredient.name
                )
            }
        }
        restocked(p.getRestaurantId())
    }

    /**
     * triggered by incident, an ingredient might become unavailable
     */
    fun markUnavailable(i: Ingredient, from: Int, duration: Int) {
        unavailableUntil[i] = Pair(from, from + duration - 1)
    }
}
