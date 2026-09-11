package de.unisaarland.cs.se.selab.sharedPackage

import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood

/** A group not bound to a restaurant that browses for one each evening it visits. */
class CasualCustomerGroup(
    id: Int,
    groupSize: Int,
    tableType: TableType,
    visitingTick: Int,
    members: List<Customer>,
    preferences: List<FoodPreference>,
    val restaurantTypes: Set<RestaurantType>,
    val visitingEvenings: List<Int>,
    deliveryDistance: Int,
    val ratingLikelihood: RatingLikelihood,
) : CustomerGroup(id, groupSize, GroupType.CASUAL, tableType, visitingTick, deliveryDistance, members, preferences) {

    override fun visitsOn(evening: Int): Boolean = visitingEvenings.contains(evening)

    override fun homeRestaurant(): Int? = null

    /** Rates per [ratingLikelihood]; a rated neutral experience is always positive. */
    override fun ratingFor(experience: Experience): Rating? = when (ratingLikelihood) {
        RatingLikelihood.NEVER -> null
        RatingLikelihood.SOME -> when (experience) {
            Experience.POSITIVE -> Rating.POSITIVE
            Experience.NEGATIVE -> Rating.NEGATIVE
            Experience.NEUTRAL -> null
        }
        RatingLikelihood.ALWAYS ->
            if (experience == Experience.NEGATIVE) Rating.NEGATIVE else Rating.POSITIVE
    }

    /** The kitchen guesses casual orders itself; this group contributes nothing fixed. */
    override fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int> = emptyMap()
}
