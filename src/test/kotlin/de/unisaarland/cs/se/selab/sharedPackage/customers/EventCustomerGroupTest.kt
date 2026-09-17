package de.unisaarland.cs.se.selab.sharedPackage.customers

import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import de.unisaarland.cs.se.selab.testsupport.Fixtures.members
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** P04: the EVENT group - booking, its always-rate rule and the imposed favourite dish. */
class EventCustomerGroupTest {

    private fun eventGroup(
        id: Int = 1,
        size: Int = 4,
        eventEvening: Int = 10,
        types: Set<RestaurantType> = setOf(RestaurantType.EUROPEAN),
        favourites: Map<RestaurantType, String> = mapOf(RestaurantType.EUROPEAN to "dish1"),
    ) = EventCustomerGroup(id, size, TableType.COMMON, 1, members(size), emptyList(), types, eventEvening, favourites)

    @Test
    fun restaurantTypesReturnsTheDeclaredSet() {
        val types = setOf(RestaurantType.EUROPEAN, RestaurantType.ASIAN)
        val group = eventGroup(types = types)

        assertEquals(types, group.restaurantTypes())
    }

    @Test
    fun getEventEveningReturnsTheDeclaredEvening() {
        assertEquals(10, eventGroup(eventEvening = 10).getEventEvening())
    }

    @Test
    fun getDeliveryDistanceIsAlwaysZero() {
        assertEquals(0, eventGroup().getDeliveryDistance())
    }

    @Test
    fun favouriteDishesReturnsTheDeclaredMap() {
        val favourites = mapOf(RestaurantType.EUROPEAN to "pasta", RestaurantType.ASIAN to "ramen")
        val group = eventGroup(favourites = favourites)

        assertEquals(favourites, group.favouriteDishes())
    }

    @Test
    fun bookedRestaurantIsNullUntilBooked() {
        val group = eventGroup()

        assertNull(group.bookedRestaurant())
    }

    @Test
    fun bookRecordsTheRestaurantId() {
        val group = eventGroup()

        group.book(5)

        assertEquals(5, group.bookedRestaurant())
    }

    @Test
    fun cancelBookingDropsTheBooking() {
        val group = eventGroup()
        group.book(5)

        group.cancelBooking()

        assertNull(group.bookedRestaurant())
    }

    @Test
    fun booksOnIsTrueExactlyThreeEveningsBeforeTheEvent() {
        val group = eventGroup(eventEvening = 10)

        assertTrue(group.booksOn(7))
        assertFalse(group.booksOn(6))
        assertFalse(group.booksOn(8))
    }

    @Test
    fun visitsOnRequiresBothTheEventEveningAndABooking() {
        val group = eventGroup(eventEvening = 10)

        assertFalse(group.visitsOn(10))

        group.book(5)

        assertTrue(group.visitsOn(10))
        assertFalse(group.visitsOn(9))
    }

    @Test
    fun homeRestaurantMirrorsTheBookingState() {
        val group = eventGroup()

        assertNull(group.homeRestaurant())

        group.book(5)
        assertEquals(5, group.homeRestaurant())

        group.cancelBooking()
        assertNull(group.homeRestaurant())
    }

    @Test
    fun ratingForNegativeExperienceIsNegative() {
        assertEquals(Rating.NEGATIVE, eventGroup().ratingFor(Experience.NEGATIVE))
    }

    @Test
    fun ratingForNeutralExperienceIsPositiveNotSilence() {
        assertEquals(Rating.POSITIVE, eventGroup().ratingFor(Experience.NEUTRAL))
    }

    @Test
    fun ratingForPositiveExperienceIsPositive() {
        assertEquals(Rating.POSITIVE, eventGroup().ratingFor(Experience.POSITIVE))
    }

    @Test
    fun ratingIsNeverNullEventGroupsAlwaysRate() {
        for (experience in Experience.entries) {
            assertEquals(true, eventGroup().ratingFor(experience) != null)
        }
    }

    @Test
    fun expectedDishesImposesTheFavouriteOnTheWholeGroupCount() {
        val favourite = recipe(1)
        val other = recipe(2)
        val group = eventGroup(size = 6, favourites = mapOf(RestaurantType.EUROPEAN to favourite.dishName))

        val expected = group.expectedDishes(listOf(other, favourite))

        assertEquals(mapOf(favourite to 6), expected)
    }

    @Test
    fun expectedDishesIsEmptyWhenTheMenuHasNoMatchingFavourite() {
        val other = recipe(2)
        val group = eventGroup(favourites = mapOf(RestaurantType.EUROPEAN to "unrelated-dish"))

        val expected = group.expectedDishes(listOf(other))

        assertTrue(expected.isEmpty())
    }

    @Test
    fun dishOverrideReturnsTheFavouriteForTheGivenType() {
        val group = eventGroup(favourites = mapOf(RestaurantType.EUROPEAN to "pasta"))

        assertEquals("pasta", group.dishOverride(RestaurantType.EUROPEAN))
    }

    @Test
    fun dishOverrideIsNullForATypeWithoutADeclaredFavourite() {
        val group = eventGroup(favourites = mapOf(RestaurantType.EUROPEAN to "pasta"))

        assertNull(group.dishOverride(RestaurantType.ASIAN))
    }

    @Test
    fun isDeliveryIsAlwaysFalse() {
        assertFalse(eventGroup().isDelivery())
    }

    @Test
    fun hasGivenUpIsAlwaysFalse() {
        assertFalse(eventGroup().hasGivenUp())
    }

    @Test
    fun groupTypeIsEvent() {
        assertEquals(GroupType.EVENT, eventGroup().groupType())
    }
}
