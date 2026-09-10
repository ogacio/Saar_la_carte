package de.unisaarland.cs.se.selab.incident

import de.unisaarland.cs.se.selab.shared.CookType
import de.unisaarland.cs.se.selab.shared.Ingredient
import de.unisaarland.cs.se.selab.shared.StaffType

/**
 * Staff joins or leaves one restaurant (specification, Section 2.2, "Staff Change").
 *
 * The change is restaurant scoped and the resulting number of staff never drops below zero, which
 * the restaurant itself enforces. [cookType] is set exactly when [staffType] is COOK and is never
 * EXEC.
 */
class StaffChange(
    id: Int,
    evening: Int,
    private val restaurantId: Int,
    private val number: Int,
    private val staffType: StaffType,
    private val cookType: CookType?,
) : Incident(id, evening) {
    override val type: IncidentType = IncidentType.STAFF

    override fun apply(sim: Simulator) {
        sim.restaurantById(restaurantId)?.changeStaff(staffType, cookType, number)
    }
}

/**
 * The amount of one ingredient changes in every recipe that uses it (specification, "Recipe
 * Change").
 *
 * The change is global and permanent: every recipe of every restaurant is adapted, and each
 * occurrence is adapted relative to its own current amount.
 */
class RecipeChange(
    id: Int,
    evening: Int,
    private val ingredient: Ingredient,
    private val adaptation: Int,
) : Incident(id, evening) {
    override val type: IncidentType = IncidentType.RECIPE

    override fun apply(sim: Simulator) {
        for (recipe in sim.allRecipes()) {
            recipe.ingredients
                .filter { it.ingredient.name == ingredient.name }
                .forEach { it.adaptBy(adaptation) }
        }
    }
}

/**
 * The supplier sells one ingredient in a different packaging (specification, "Packaging Change").
 *
 * The ingredient object is shared by every recipe and pantry that refers to it, so changing it once
 * is enough to reach the whole simulation.
 */
class PackagingChange(
    id: Int,
    evening: Int,
    private val ingredient: Ingredient,
    private val packagingVolume: Int,
) : Incident(id, evening) {
    override val type: IncidentType = IncidentType.PACKAGING

    override fun apply(sim: Simulator) {
        if (packagingVolume <= 0) {
            return
        }
        ingredient.changePackaging(packagingVolume)
    }
}

/**
 * One ingredient cannot be bought for a number of evenings (specification, "Ingredient
 * Unavailability").
 *
 * The unavailability starts with the evening the incident occurs and covers [duration] evenings
 * including that one. Two unavailabilities of the same ingredient never overlap.
 */
class IngredientUnavailability(
    id: Int,
    evening: Int,
    private val ingredient: Ingredient,
    private val duration: Int,
) : Incident(id, evening) {
    override val type: IncidentType = IncidentType.UNAVAILABLE

    override fun apply(sim: Simulator) {
        Supplier.markUnavailable(ingredient, evening, duration)
    }
}
