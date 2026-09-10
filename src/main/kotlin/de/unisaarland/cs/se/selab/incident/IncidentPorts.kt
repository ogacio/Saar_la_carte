package de.unisaarland.cs.se.selab.incident

import de.unisaarland.cs.se.selab.shared.CookType
import de.unisaarland.cs.se.selab.shared.Ingredient
import de.unisaarland.cs.se.selab.shared.Recipe
import de.unisaarland.cs.se.selab.shared.StaffType

/**
 * A restaurant whose staff a STAFF incident can change.
 *
 * `Restaurant` implements this; the incident package never needs anything else from a restaurant.
 */
interface StaffChangeable {
    /**
     * Changes the number of staff of [type] by [delta], for cooks of [cook], never below zero.
     */
    fun changeStaff(type: StaffType, cook: CookType?, delta: Int)
}

/**
 * The supplier side an UNAVAILABLE incident acts on.
 *
 * `Supplier` implements this.
 */
interface IngredientAvailability {
    /**
     * Makes [ingredient] unavailable for [duration] evenings, starting with evening [from].
     */
    fun markUnavailable(ingredient: Ingredient, from: Int, duration: Int)
}

/**
 * What an [Incident] needs from the running simulation.
 *
 * `Simulator` implements this. An incident takes the whole simulation rather than one restaurant
 * because only the STAFF incident is restaurant scoped: RECIPE reaches every restaurant, PACKAGING
 * changes a shared ingredient and UNAVAILABLE acts on the global supplier.
 */
interface SimulationContext {
    /** The supplier all restaurants procure from. */
    val supplier: IngredientAvailability

    /**
     * The restaurant with [id], or null if the simulation does not know it.
     */
    fun restaurantById(id: Int): StaffChangeable?

    /**
     * Every recipe of every restaurant in the simulation.
     */
    fun allRecipes(): List<Recipe>
}
