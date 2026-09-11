package de.unisaarland.cs.se.selab.customer

import de.unisaarland.cs.se.selab.sharedPackage.Customer
import de.unisaarland.cs.se.selab.sharedPackage.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.FoodPreference
import de.unisaarland.cs.se.selab.sharedPackage.GroupType
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating

/**
 * A group that books a restaurant for one event evening (specification, Section 2.2, "EVENTS").
 *
 * An event group has no home restaurant. It browses three evenings before its event, in the first
 * tick of that evening, so that the restaurant can reserve tables and buy ingredients in time; that
 * is why the earliest possible event evening is the fourth. It has decided on one favourite basic
 * dish per restaurant type it considers, which it recommends to all of its members, and it always
 * leaves a rating.
 */
class EventCustomerGroup(
    id: Int,
    groupSize: Int,
    tableType: TableType,
    visitingTick: Int,
    members: List<Customer>,
    preferences: List<FoodPreference>,
    val restaurantTypes: Set<RestaurantType>,
    val eventEvening: Int,
    val favouriteDishes: Map<RestaurantType, String>,
) : CustomerGroup(id, groupSize, GroupType.EVENT, tableType, visitingTick, null, members, preferences) {

    /** The restaurant the group booked, null until it browsed successfully. */
    var bookedRestaurant: Int? = null
        private set

    /**
     * Whether the group makes its reservation on [evening], three evenings before the event.
     */
    fun booksOn(evening: Int): Boolean = evening == eventEvening - BOOKING_LEAD

    /**
     * Records that the group booked restaurant [restaurantId] for its event evening.
     */
    fun book(restaurantId: Int) {
        bookedRestaurant = restaurantId
    }

    /**
     * Drops the booking after the restaurant cancelled the reservation.
     */
    fun cancelBooking() {
        bookedRestaurant = null
    }

    override fun visitsOn(evening: Int): Boolean = evening == eventEvening && bookedRestaurant != null

    override fun homeRestaurant(): Int? = bookedRestaurant

    /**
     * Event groups always rate: a negative experience gives a negative rating, everything else a
     * positive one.
     */
    override fun ratingFor(experience: Experience): Rating? =
        if (experience == Experience.NEGATIVE) Rating.NEGATIVE else Rating.POSITIVE

    /**
     * The whole group orders the favourite dish of the event, so the restaurant prepares that dish
     * for every member.
     */
    override fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int> {
        val favourites = favouriteDishes.values.toSet()
        val dish = menu.firstOrNull { favourites.contains(it.dishName) } ?: return emptyMap()
        return mapOf(dish to groupSize)
    }

    override fun dishOverride(type: RestaurantType): String? = favouriteDishes[type]

    override fun isDelivery(): Boolean = false

    override fun hasGivenUp(): Boolean = false

    /** How many evenings before the event the group makes its reservation. */
    private companion object {
        const val BOOKING_LEAD = 3
    }
}
