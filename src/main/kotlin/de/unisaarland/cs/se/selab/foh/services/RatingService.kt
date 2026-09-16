package de.unisaarland.cs.se.selab.foh.services

import de.unisaarland.cs.se.selab.foh.visit.Visit
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.Statistics
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook

/**
 * Step 7: the rating step at the end of a tick (spec, "Ratings").
 *
 * [rate] handles a group whose visit ended this tick, [rateFailedReservation] a group
 * whose table reservation failed; that group rates in the first tick of the evening.
 * The FOH calls them for all such groups together, in group order, and then [logStatus] once.
 */
class RatingService(
    private val ratings: RatingBook,
) {
    private var rated = 0

    /** Normal rating: the group of a finished [visit] judges how the visit went. */
    fun rate(visit: Visit, sbu: SubUnits) {
        visit.markRated()
        recordAttempt(visit.group, visit.failedAttempt)
        leaveRating(visit.group, visit.experience(), sbu)
    }

    /**
     * Rating after a failed reservation: the group never arrived. The spec counts this as
     * a negative experience and, for a REGULAR group, as a failed attempt.
     */
    fun rateFailedReservation(group: CustomerGroup, sbu: SubUnits) {
        recordAttempt(group, failed = true)
        leaveRating(group, Experience.NEGATIVE, sbu)
    }

    /** Writes how many groups rated this tick and starts counting afresh. */
    fun logStatus(sbu: SubUnits) {
        Logger.Customer.ratingStatus(sbu.restaurantId, rated)
        rated = 0
    }

    /**
     * The group turns [experience] into a rating, or into none: REGULAR and EVENT groups
     * always rate, CASUAL groups by their rating likelihood. A rating is stored, counted
     * for the statistics and logged with the restaurant's new totals.
     */
    private fun leaveRating(group: CustomerGroup, experience: Experience, sbu: SubUnits) {
        val rating = group.ratingFor(experience) ?: return
        ratings.addRating(sbu.restaurantId, rating)
        Statistics.recordRating(sbu.restaurantId)
        val score = ratings.getById(sbu.restaurantId)
        Logger.Customer.rating(sbu.restaurantId, group.id(), rating, score.positiveRatings, score.negativeRatings)
        rated++
    }

    /** REGULAR groups stop coming after two failed attempts in a row; any other visit resets the streak. */
    private fun recordAttempt(group: CustomerGroup, failed: Boolean) {
        if (group !is RegularCustomerGroup) return
        if (failed) group.recordFailedAttempt() else group.recordSuccessfulVisit()
    }
}
