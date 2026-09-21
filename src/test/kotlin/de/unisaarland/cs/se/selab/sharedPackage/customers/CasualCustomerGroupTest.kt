package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * F23: CasualCustomerGroup: specific visiting evenings, dynamic restaurant selection, and variable rating likelihood.
*/
class CasualCustomerGroupTest {

    private fun group(
        id: Int = 1,
        groupSize: Int = 2,
        visitingEvenings: List<Int> = listOf(2, 5, 8),
        ratingLikelihood: RatingLikelihood = RatingLikelihood.ALWAYS,
        restaurantTypes: Set<RestaurantType> = setOf(RestaurantType.ASIAN),
        deliveryDistance: Int = 0
    ) = CasualCustomerGroup(
        id = id,
        groupSize = groupSize,
        tableType = TableType.COMMON,
        visitingTick = 1,
        members = List(groupSize) { Customer(null) },
        preferences = emptyList(),
        restaurantTypes = restaurantTypes,
        visitingEvenings = visitingEvenings,
        deliveryPreference = DeliveryPreference(deliveryDistance, ratingLikelihood) // [cite: 10]
    )

    /** Visits only occur on the explicitly defined visiting evenings[cite: 10]. */
    @Test
    fun visitsOnlyOnScheduledEvenings() {
        val g = group(visitingEvenings = listOf(2, 5, 8))

        assertFalse(g.visitsOn(1))
        assertTrue(g.visitsOn(2))
        assertFalse(g.visitsOn(3))
        assertFalse(g.visitsOn(4))
        assertTrue(g.visitsOn(5))
        assertFalse(g.visitsOn(6))
        assertFalse(g.visitsOn(7))
        assertTrue(g.visitsOn(8))
    }

    /** RatingLikelihood.NEVER produces no ratings regardless of experience[cite: 10]. */
    @Test
    fun neverRatingLikelihoodProducesNoRatings() {
        val g = group(ratingLikelihood = RatingLikelihood.NEVER)

        assertNull(g.ratingFor(Experience.NEGATIVE))
        assertNull(g.ratingFor(Experience.NEUTRAL))
        assertNull(g.ratingFor(Experience.POSITIVE))
    }

    /** RatingLikelihood.SOME rates positive and negative experiences, but ignores neutral[cite: 10]. */
    @Test
    fun someRatingLikelihoodRatesPositiveAndNegativeButNotNeutral() {
        val g = group(ratingLikelihood = RatingLikelihood.SOME)

        assertEquals(Rating.NEGATIVE, g.ratingFor(Experience.NEGATIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.POSITIVE))
        assertNull(g.ratingFor(Experience.NEUTRAL))
    }

    /** RatingLikelihood.ALWAYS rates positive/negative normally, and neutral as positive[cite: 10]. */
    @Test
    fun alwaysRatingLikelihoodRatesAllExperiences() {
        val g = group(ratingLikelihood = RatingLikelihood.ALWAYS)

        assertEquals(Rating.NEGATIVE, g.ratingFor(Experience.NEGATIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.POSITIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.NEUTRAL))
    }

    /** CASUAL groups are not bound to a fixed restaurant and use restaurant types to browse instead[cite: 10]. */
    @Test
    fun isNotBoundToHomeRestaurantAndSearchesByTypes() {
        val types = setOf(RestaurantType.EUROPEAN, RestaurantType.AFRICAN)
        val g = group(restaurantTypes = types)

        assertNull(g.homeRestaurant())
        assertEquals(types, g.restaurantTypes())
        assertEquals(0, g.getEventEvening())
    }

    /** For F23 in-person casual visits,
     * the delivery distance defaults to 0 and evaluates as non-delivery[cite: 3, 10]. */
    @Test
    fun inPersonVisitHasZeroDeliveryDistance() {
        val g = group(deliveryDistance = 0)

        assertEquals(0, g.getDeliveryDistance())
        assertFalse(g.isDelivery())
    }

    /** The kitchen guesses casual orders itself;
     * CASUAL groups contribute nothing fixed to expected dishes[cite: 10]. */
    @Test
    fun expectedDishesReturnsEmptyMap() {
        val g = group()
        val dummyMenu = listOf(Recipe(1, "dish1", 10, emptySet(), mutableListOf(), null))

        val expected = g.expectedDishes(dummyMenu, RestaurantType.ASIAN)

        assertTrue(expected.isEmpty())
    }
}
