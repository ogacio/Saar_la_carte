package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests EventCustomerGroup's booking lifecycle, rating and favourite-dish rules.
 * Covers F25.
 */

class F25EventCustomerGroupTest {

    private fun group(
        eventEvening: Int = 30,
        favouriteDishes: Map<RestaurantType, String> = mapOf(RestaurantType.EUROPEAN to "Potato Soup"),
    ) = EventCustomerGroup(
        1, 10, TableType.COMMON, 4, emptyList(), emptyList(),
        setOf(RestaurantType.EUROPEAN), eventEvening, favouriteDishes,
    )

    @Test
    fun eventGroupBooksOnExactlyThreeEveningsBeforeEvent() {
        val g = group(eventEvening = 30)
        assertTrue(g.booksOn(27))
        assertTrue(!g.booksOn(28))
        assertTrue(!g.booksOn(30))
    }

    @Test
    fun eventGroupVisitsOnlyOnEventEveningWithConfirmedBooking() {
        val g = group(eventEvening = 30)
        assertTrue(!g.visitsOn(30))
        g.book(1)
        assertTrue(g.visitsOn(30))
        assertTrue(!g.visitsOn(31))
    }

    @Test
    fun eventGroupCancelBookingClearsRestaurantAndStopsVisiting() {
        val g = group(eventEvening = 30)
        g.book(1)
        assertEquals(1, g.bookedRestaurant())
        g.cancelBooking()
        assertNull(g.bookedRestaurant())
        assertTrue(!g.visitsOn(30))
    }

    @Test
    fun eventGroupHomeRestaurantMirrorsBookedRestaurant() {
        val g = group(eventEvening = 30)
        assertNull(g.homeRestaurant())
        g.book(7)
        assertEquals(7, g.homeRestaurant())
    }

    @Test
    fun eventGroupRatingIsNegativeOnlyForNegativeExperience() {
        val g = group()
        assertEquals(Rating.NEGATIVE, g.ratingFor(Experience.NEGATIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.NEUTRAL))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.POSITIVE))
    }

    @Test
    fun eventGroupExpectedDishesReturnsFavouriteForMatchingTypeAtGroupSize() {
        val g = group(favouriteDishes = mapOf(RestaurantType.EUROPEAN to "Potato Soup"))
        val soup: Recipe = mock()
        whenever(soup.getDishName()).thenReturn("Potato Soup")
        val other: Recipe = mock()
        whenever(other.getDishName()).thenReturn("Chicken Rice")

        val expected = g.expectedDishes(listOf(other, soup), RestaurantType.EUROPEAN)
        assertEquals(mapOf(soup to 10), expected)
    }

    @Test
    fun eventGroupExpectedDishesEmptyWhenTypeHasNoFavourite() {
        val g = group(favouriteDishes = mapOf(RestaurantType.EUROPEAN to "Potato Soup"))
        assertTrue(g.expectedDishes(emptyList(), RestaurantType.ASIAN).isEmpty())
    }

    @Test
    fun eventGroupExpectedDishesEmptyWhenFavouriteNotOnMenu() {
        val g = group(favouriteDishes = mapOf(RestaurantType.EUROPEAN to "Potato Soup"))
        assertTrue(g.expectedDishes(emptyList(), RestaurantType.EUROPEAN).isEmpty())
    }

    @Test
    fun eventGroupDishOverrideReturnsFavouriteNameOrNull() {
        val g = group(favouriteDishes = mapOf(RestaurantType.EUROPEAN to "Potato Soup"))
        assertEquals("Potato Soup", g.dishOverride(RestaurantType.EUROPEAN))
        assertNull(g.dishOverride(RestaurantType.ASIAN))
    }
}
