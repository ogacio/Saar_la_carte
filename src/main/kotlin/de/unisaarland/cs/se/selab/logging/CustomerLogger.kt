package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.shared.Rating

/**
 * The statements about what the customer groups do: deciding on a restaurant, arriving, eating and
 * rating.
 */
object CustomerLogger {
    private fun tag(restaurantId: Int) = LogFormat.restaurantTag(restaurantId)

    /**
     * Reports that group [groupId] decided on restaurant [restaurantId].
     */
    fun restaurantDecision(groupId: Int, restaurantId: Int) =
        LogSink.write(LogLevel.DEBUG, "Restaurant Decision: Group $groupId decided on restaurant $restaurantId.")

    /**
     * Reports that group [groupId] could not decide for a restaurant.
     */
    fun noRestaurantDecision(groupId: Int) =
        LogSink.write(LogLevel.DEBUG, "Restaurant No Decision: Group $groupId could not decide for a restaurant.")

    /**
     * Reports that group [groupId] arrived at restaurant [restaurantId].
     */
    fun arrival(restaurantId: Int, groupId: Int) = LogSink.write(
        LogLevel.INFO,
        "Restaurant Arrival ${tag(restaurantId)}: Group $groupId arrived at restaurant $restaurantId.",
    )

    /**
     * Reports that [customers] customers of group [groupId] leave table [tableId] unserved.
     */
    fun noEating(restaurantId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
        LogLevel.INFO,
        "Restaurant No Eating ${tag(restaurantId)}: $customers customers of group $groupId " +
            "leave table $tableId due to not being served.",
    )

    /**
     * Reports that [customers] customers of group [groupId] finished eating at table [tableId].
     */
    fun finishedEating(restaurantId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
        LogLevel.INFO,
        "FOH Finished Eating ${tag(restaurantId)}: $customers customers of group $groupId " +
            "have finished eating at table $tableId.",
    )

    /**
     * Reports how many customers are still [eating] and how many have [eaten] this tick.
     */
    fun eatingStatus(restaurantId: Int, eating: Int, eaten: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH Eating Status ${tag(restaurantId)}: $eating customers are eating and " +
            "$eaten customers have finished eating this tick.",
    )

    /**
     * Reports the [rating] group [groupId] left and the resulting rating counts of the restaurant.
     */
    fun rating(restaurantId: Int, groupId: Int, rating: Rating, positiveRatings: Int, negativeRatings: Int) =
        LogSink.write(
            LogLevel.INFO,
            "Rating ${tag(restaurantId)}: Group $groupId rates the restaurant $restaurantId with $rating rating, " +
                "leading to $positiveRatings positive ratings and $negativeRatings negative ratings.",
        )

    /**
     * Reports how many [groups] performed a rating this tick.
     */
    fun ratingStatus(restaurantId: Int, groups: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Rating Status ${tag(restaurantId)}: $groups groups performed ratings this tick.",
    )
}
