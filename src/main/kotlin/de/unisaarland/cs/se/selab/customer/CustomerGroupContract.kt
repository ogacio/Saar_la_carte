package de.unisaarland.cs.se.selab.customer

import de.unisaarland.cs.se.selab.shared.Experience
import de.unisaarland.cs.se.selab.shared.GroupType
import de.unisaarland.cs.se.selab.shared.Rating
import de.unisaarland.cs.se.selab.shared.Recipe
import de.unisaarland.cs.se.selab.shared.RestaurantType
import de.unisaarland.cs.se.selab.shared.TableType

/**
 * What the rest of the simulation needs from a customer group, independent of its type.
 *
 * The abstract `CustomerGroup` of the shared package implements this, so that a concrete group such
 * as [EventCustomerGroup] can be written and tested against the contract alone.
 */
interface CustomerGroupContract {
    /** The unique id of the group. */
    val id: Int

    /** How many customers the group has. */
    val groupSize: Int

    /** Whether the group is REGULAR, CASUAL or an EVENT. */
    val groupType: GroupType

    /** The kind of table the group wants to sit on. */
    val tableType: TableType

    /** The tick of the evening at which the group wants its food. */
    val visitingTick: Int

    /**
     * Whether the group gets food from a restaurant on [evening].
     */
    fun visitsOn(evening: Int): Boolean

    /**
     * The restaurant the group is bound to, or null if it browses for one.
     */
    fun homeRestaurant(): Int?

    /**
     * The rating the group leaves after [experience], or null if it does not rate at all.
     */
    fun ratingFor(experience: Experience): Rating?

    /**
     * How many meals of which recipe of [menu] the group is expected to order.
     */
    fun expectedDishes(menu: List<Recipe>): Map<Recipe, Int>

    /**
     * The dish the whole group orders in a restaurant of [type], or null if everybody chooses.
     */
    fun dishOverride(type: RestaurantType): String?

    /**
     * Whether the group wants its food delivered instead of visiting the restaurant.
     */
    fun isDelivery(): Boolean

    /**
     * Whether the group stopped visiting restaurants after repeated failures.
     */
    fun hasGivenUp(): Boolean
}
