package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
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
    private val restaurantTypes: Set<RestaurantType>,
    private val visitingEvenings: List<Int>,
    deliveryDistance: Int,
    private val ratingLikelihood: RatingLikelihood,
) : CustomerGroup(id, groupSize, GroupType.CASUAL, tableType, visitingTick, deliveryDistance, members, preferences) {

    /** The restaurant types this group is willing to visit. */
    override fun restaurantTypes(): Set<RestaurantType> = restaurantTypes

    /** The evenings this group intends to get food from a restaurant. */
    fun visitingEvenings(): List<Int> = visitingEvenings

    /** How readily this group leaves a rating after an experience. */
    fun ratingLikelihood(): RatingLikelihood = ratingLikelihood

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
    override fun getDeliveryDistance():Int = deliveryDistance

    /** The kitchen guesses casual orders itself; this group contributes nothing fixed. */
    override fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int> = emptyMap()
}
