package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.testsupport.Fixtures.casual
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** F28: the browsing service picks the best eligible restaurant and books its capacity. */
class BrowsingServiceTest {

    private val ratings = RatingBook
    private val fish = Ingredient("fish", UnitType.G, 100, 1)

    /** Sets the clock to tick [tick] of a fresh evening. */
    private fun atTick(tick: Int) {
        GlobalClock.advanceEvening()
        repeat(tick) { GlobalClock.advanceTick() }
    }

    private fun rated(restaurantId: Int, positive: Int, negative: Int = 0) {
        RatingBook.initializeRatings(restaurantId, positive, negative)
    }

    private fun restaurant(
        id: Int,
        type: RestaurantType = RestaurantType.EUROPEAN,
        seats: Int = 10,
        drivers: Int = 1,
        hostsEvents: Boolean = false,
        dishes: List<Recipe> = listOf(recipe(1)),
    ) = RestaurantData(
        id,
        type,
        2,
        20,
        dishes.toMutableList(),
        mutableMapOf(TableType.COMMON to seats),
        drivers,
        hostsEvents,
        seats,
        mutableMapOf(),
    )

    @BeforeTest
    fun openRestaurants() {
        atTick(5)
        (1..4).forEach { rated(it, 0) }
    }

    @Test
    fun restaurantOfAnotherTypeIsNotChosen() {
        val service = BrowsingService(mutableListOf(restaurant(1, type = RestaurantType.ASIAN)), ratings)

        assertNull(service.choose(casual(1, 2)))
    }

    @Test
    fun closedRestaurantIsNotChosen() {
        val service = BrowsingService(mutableListOf(restaurant(1)), ratings)
        atTick(1)

        assertNull(service.choose(casual(1, 2)))
    }
//
//    @Test
//    fun restaurantDoesNotAcceptNewCustomersInTheLastThreeTicks() {
//        val service = BrowsingService(mutableListOf(restaurant(1)), ratings)
//        atTick(18)
//
//        assertNull(service.choose(casual(1, 2)))
//    }

    @Test
    fun restaurantWithoutAnyEdibleDishIsNotChosen() {
        val fishDish = recipe(1, listOf(RecipeIngredient(fish, 50)))
        val service = BrowsingService(mutableListOf(restaurant(1, dishes = listOf(fishDish))), ratings)
        val noFish = FoodPreference(2, setOf(fish), emptySet(), emptyList())

        assertNull(service.choose(casual(1, 2, preference = noFish)))
    }

    @Test
    fun restaurantWithOneEdibleDishIsChosen() {
        val fishDish = recipe(1, listOf(RecipeIngredient(fish, 50)))
        val salad = recipe(2)
        val service = BrowsingService(mutableListOf(restaurant(1, dishes = listOf(fishDish, salad))), ratings)
        val noFish = FoodPreference(2, setOf(fish), emptySet(), emptyList())

        assertEquals(1, service.choose(casual(1, 2, preference = noFish)))
    }

    @Test
    fun restaurantWithoutEnoughSeatsIsNotChosenForEatIn() {
        val service = BrowsingService(mutableListOf(restaurant(1, seats = 3)), ratings)

        assertNull(service.choose(casual(1, 4)))
    }

    @Test
    fun restaurantWithoutSeatsOfTheWantedTableTypeIsNotChosen() {
        val service = BrowsingService(mutableListOf(restaurant(1)), ratings)

        assertNull(service.choose(casual(1, 2, tableType = TableType.BAR)))
    }

    @Test
    fun restaurantWithoutFreeDriverIsNotChosenForDelivery() {
        val service = BrowsingService(mutableListOf(restaurant(1, drivers = 0)), ratings)

        assertNull(service.choose(casual(1, 4, deliveryDistance = 5)))
    }

    @Test
    fun deliveryGroupBooksADriverOfTheChosenRestaurant() {
        val snapshot = restaurant(1, seats = 0, drivers = 1)
        val service = BrowsingService(mutableListOf(snapshot), ratings)

        assertEquals(1, service.choose(casual(1, 4, deliveryDistance = 5)))
        assertEquals(0, snapshot.getFreeDrivers())
    }

    @Test
    fun highestRatingDifferenceWinsAndItsSeatsAreBooked() {
        val low = restaurant(1)
        val high = restaurant(2)
        rated(1, positive = 5, negative = 4)
        rated(2, positive = 3, negative = 0)
        val service = BrowsingService(mutableListOf(low, high), ratings)

        assertEquals(2, service.choose(casual(1, 4)))
        assertEquals(6, high.getFreeSeats()[TableType.COMMON])
        assertEquals(10, low.getFreeSeats()[TableType.COMMON])
    }

    @Test
    fun tieGoesToTheLowestId() {
        rated(3, positive = 2)
        rated(4, positive = 2)
        val service = BrowsingService(mutableListOf(restaurant(4), restaurant(3)), ratings)

        assertEquals(3, service.choose(casual(1, 2)))
    }

    @Test
    fun bookedSeatsAreSeenByTheNextGroup() {
        val service = BrowsingService(mutableListOf(restaurant(1, seats = 5)), ratings)

        assertEquals(1, service.choose(casual(1, 4)))
        assertNull(service.choose(casual(2, 2)))
    }

    @Test
    fun refreshReplacesTheSnapshots() {
        val service = BrowsingService(mutableListOf(restaurant(1, seats = 0)), ratings)

        service.refresh(mutableListOf(restaurant(1, seats = 4)))

        assertEquals(1, service.choose(casual(1, 4)))
    }

    @Test
    fun eventGroupOnlyConsidersRestaurantsHostingEvents() {
        val service = BrowsingService(mutableListOf(restaurant(1, hostsEvents = false)), ratings)

        assertNull(service.chooseForEvent(event(1, 4, eventEvening = 7), 7))
    }

    @Test
    fun eventGroupNeedsTheRestaurantOpenAtItsVisitingTick() {
        val service = BrowsingService(mutableListOf(restaurant(1, hostsEvents = true)), ratings)

        assertNull(service.chooseForEvent(event(1, 4, eventEvening = 7, visitingTick = 21), 7))
    }

    @Test
    fun eventGroupOnlyConsidersRestaurantsWithAnEdibleDish() {
        val fishDish = recipe(1, listOf(RecipeIngredient(fish, 50)))
        val noFish = FoodPreference(4, setOf(fish), emptySet(), emptyList())
        val onlyFish = restaurant(1, hostsEvents = true, dishes = listOf(fishDish))
        val alsoSalad = restaurant(2, hostsEvents = true, dishes = listOf(fishDish, recipe(2)))
        val service = BrowsingService(mutableListOf(onlyFish, alsoSalad), ratings)

        assertEquals(2, service.chooseForEvent(event(1, 4, eventEvening = 7, visitingTick = 5, preference = noFish), 7))
    }

    @Test
    fun ratingsAndEntriesCanBeReplaced() {
        val service = BrowsingService(mutableListOf(), RatingBook)

        service.entries = mutableListOf(restaurant(1))
        service.ratings = ratings

        assertEquals(1, service.choose(casual(1, 2)))
    }

    @Test
    fun eventGroupBooksEventSeatsForItsEveningOnly() {
        val snapshot = restaurant(1, seats = 10, hostsEvents = true)
        val service = BrowsingService(mutableListOf(snapshot), ratings)

        assertEquals(1, service.chooseForEvent(event(1, 6, eventEvening = 7, visitingTick = 5), 7))
        assertNull(service.chooseForEvent(event(2, 6, eventEvening = 7, visitingTick = 5), 7))
        assertEquals(1, service.chooseForEvent(event(3, 6, eventEvening = 8, visitingTick = 5), 8))
        assertEquals(4, snapshot.eventSeatsLeft(7))
    }
}
