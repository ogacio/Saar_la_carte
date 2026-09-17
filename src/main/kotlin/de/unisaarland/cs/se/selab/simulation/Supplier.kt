package de.unisaarland.cs.se.selab.simulation
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
        for ((ingredient, amount) in needed) {
            val evening = GlobalClock.getEvening()
            // if the ingredient is available, we supply it
            if (!(
                    unavailableUntil.containsKey(ingredient) &&
                        evening >= checkNotNull(unavailableUntil[ingredient]).first &&
                        checkNotNull(unavailableUntil[ingredient]).second <= evening
                    )
            ) {
                p.restock(ingredient, ingredient.packagesFor(amount) * amount)
            }
        }
    }

    /**
     * triggered by incident, an ingredient might become unavailable
     */
    fun markUnavailable(i: Ingredient, from: Int, duration: Int) {
        unavailableUntil[i] = Pair(from, from + duration - 1)
    }
}
