package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood

/** How far a CASUAL group orders delivery from, and how readily it rates its experience. */
data class DeliveryPreference(val distance: Int, val ratingLikelihood: RatingLikelihood)

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
    private val deliveryPreference: DeliveryPreference,
) : CustomerGroup(
    id,
    groupSize,
    GroupType.CASUAL,
    tableType,
    visitingTick,
    deliveryPreference.distance,
    members,
    preferences,
) {

    private var deliveryOrderPending: Boolean = false
    private var deliveryGivenUp: Boolean = false

    /** The restaurant types this group is willing to visit. */
    override fun restaurantTypes(): Set<RestaurantType> = restaurantTypes

    /** The evenings this group intends to get food from a restaurant. */
    fun visitingEvenings(): List<Int> = visitingEvenings

    /** How readily this group leaves a rating after an experience. */
    fun ratingLikelihood(): RatingLikelihood = deliveryPreference.ratingLikelihood

    override fun visitsOn(evening: Int): Boolean = visitingEvenings.contains(evening)

    override fun homeRestaurant(): Int? = null

    /** Rates per [ratingLikelihood]; a rated neutral experience is always positive. */
    override fun ratingFor(experience: Experience): Rating? = when (deliveryPreference.ratingLikelihood) {
        RatingLikelihood.NEVER -> null
        RatingLikelihood.SOME -> when (experience) {
            Experience.POSITIVE -> Rating.POSITIVE
            Experience.NEGATIVE -> Rating.NEGATIVE
            Experience.NEUTRAL -> null
        }
        RatingLikelihood.ALWAYS ->
            if (experience == Experience.NEGATIVE) Rating.NEGATIVE else Rating.POSITIVE
    }
    override fun getDeliveryDistance(): Int = deliveryDistance() ?: 0

    /** CASUAL groups have no event evening. */
    override fun getEventEvening(): Int = 0

    /** The kitchen guesses casual orders itself; this group contributes nothing fixed. */
    override fun expectedDishes(menu: List<Recipe>, type: RestaurantType): Map<Recipe, Int> = emptyMap()

    /** Marks that a delivery order was placed this evening and is now awaited. */
    override fun orderPlaced() {
        deliveryOrderPending = true
        deliveryGivenUp = false
    }

    /** Marks a placed delivery order as resolved, whether delivered or given up on. */
    override fun orderResolved() {
        deliveryOrderPending = false
    }

    /**
     * The group gives up in the third tick after [visitingTick], not after it: the rating rule says
     * they rate "in case they didn't get food at the end of the 3rd tick after the tick where their
     * food should have arrived", so the decision is made in that tick itself.
     */
    override fun deliveryGiveUpDue(): Boolean =
        deliveryOrderPending && GlobalClock.getTickInEvening() >= visitingTick() + GIVE_UP_DELAY_TICKS

    /** The time-based due check is kept for existing callers that ask whether the pending order timed out. */
    override fun hasGivenUp(): Boolean = deliveryGiveUpDue()

    /** Remember the actual give-up separately, so a driver arriving later must still fail. */
    override fun markDeliveryGivenUp() {
        deliveryGivenUp = true
        deliveryOrderPending = false
    }

    override fun deliveryWasGivenUp(): Boolean = deliveryGivenUp

    private companion object {
        const val GIVE_UP_DELAY_TICKS = 3
    }
}
