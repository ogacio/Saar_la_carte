package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingScore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * F25: Deciding on restaurants for causal groups
 */
class F25BrowsingServiceTest {

    private fun ingredient(name: String) = Ingredient(name, UnitType.G, 1000, 5)

    private fun dishOf(vararg ingredients: Ingredient): Recipe {
        val recipeIngredients = ingredients.map { RecipeIngredient(it, 1) }.toMutableList()
        return Recipe(1, "dish", 10, setOf(de.unisaarland.cs.se.selab.kitchen.CookType.EXEC), recipeIngredients, null)
    }

    /**
     * One fully-specified default RestaurantData. Every test derives its own case with .copy(...),
     * overriding only the fields it cares about, instead of a long-parameter-list helper function.
     */
    private val defaultData = RestaurantData(
        id = 1,
        type = RestaurantType.EUROPEAN,
        openingTick = 1,
        closingTick = 24,
        dishes = listOf(dishOf()),
        freeSeatsStatic = mapOf(TableType.COMMON to 10),
        freeDrivers = 5,
        hostsEvents = true,
        totalSeats = 20,
        eventSeatsBookedStatic = emptyMap(),
    )

    private fun casualGroup(
        types: Set<RestaurantType> = setOf(RestaurantType.EUROPEAN),
        isDelivery: Boolean = false,
        tableType: TableType = TableType.COMMON,
        size: Int = 4,
        preferences: List<FoodPreference> = emptyList(),
    ): CustomerGroup {
        val g: CustomerGroup = mock()
        whenever(g.restaurantTypes()).thenReturn(types)
        whenever(g.isDelivery()).thenReturn(isDelivery)
        whenever(g.tableType()).thenReturn(tableType)
        whenever(g.groupSize()).thenReturn(size)
        whenever(g.preferences()).thenReturn(preferences)
        return g
    }

    private fun eventGroupMock(
        types: Set<RestaurantType> = setOf(RestaurantType.EUROPEAN),
        visitingTick: Int = 5,
        size: Int = 10,
        preferences: List<FoodPreference> = emptyList(),
    ): CustomerGroup {
        val g: CustomerGroup = mock()
        whenever(g.restaurantTypes()).thenReturn(types)
        whenever(g.visitingTick()).thenReturn(visitingTick)
        whenever(g.preferences()).thenReturn(preferences)
        whenever(g.groupSize()).thenReturn(size)
        return g
    }

    private fun ratings(scores: Map<Int, Int>): RatingBook {
        val book: RatingBook = mock()
        for ((id, score) in scores) {
            val rs: RatingScore = mock()
            whenever(rs.score()).thenReturn(score)
            whenever(book.getById(id)).thenReturn(rs)
        }
        return book
    }

    @Test
    fun browsingChooseFiltersOutWrongRestaurantType() {
        val wrongType = defaultData.copy(id = 1, type = RestaurantType.ASIAN)
        val service = BrowsingService(mutableListOf(wrongType), ratings(mapOf(1 to 5)))
        val g = casualGroup(types = setOf(RestaurantType.EUROPEAN))

        assertNull(service.choose(g))
    }

    @Test
    fun browsingChooseFiltersOutRestaurantWithNoDishFreeOfExcludedIngredient() {
        val onion = ingredient("onion")
        val badDish = dishOf(onion)
        val restaurant = defaultData.copy(id = 1, dishes = listOf(badDish))
        val preference: FoodPreference = mock()
        whenever(preference.excluded()).thenReturn(setOf(onion))
        val service = BrowsingService(mutableListOf(restaurant), ratings(mapOf(1 to 5)))
        val g = casualGroup(preferences = listOf(preference))

        assertNull(service.choose(g))
    }

    @Test
    fun browsingChooseKeepsRestaurantWithAtLeastOneSafeDish() {
        val onion = ingredient("onion")
        val badDish = dishOf(onion)
        val safeDish = dishOf(ingredient("rice"))
        val restaurant = defaultData.copy(id = 1, dishes = listOf(badDish, safeDish))
        val preference: FoodPreference = mock()
        whenever(preference.excluded()).thenReturn(setOf(onion))
        val service = BrowsingService(mutableListOf(restaurant), ratings(mapOf(1 to 5)))
        val g = casualGroup(preferences = listOf(preference))

        assertEquals(1, service.choose(g))
    }

    @Test
    fun browsingChooseForDeliveryFiltersByFreeDriversNotSeats() {
        val noDrivers = defaultData.copy(id = 1, freeDrivers = 0)
        val service = BrowsingService(mutableListOf(noDrivers), ratings(mapOf(1 to 5)))
        val g = casualGroup(isDelivery = true)

        assertNull(service.choose(g))
    }

    @Test
    fun browsingChooseForDineInFiltersBySeatsOfRequestedTableType() {
        val tooFewSeats = defaultData.copy(id = 1, freeSeatsStatic = mapOf(TableType.COMMON to 2))
        val service = BrowsingService(mutableListOf(tooFewSeats), ratings(mapOf(1 to 5)))
        val g = casualGroup(isDelivery = false, tableType = TableType.COMMON, size = 4)

        assertNull(service.choose(g))
    }

    @Test
    fun browsingChoosePicksHighestRatedRestaurant() {
        val low = defaultData.copy(id = 1)
        val high = defaultData.copy(id = 2)
        val service = BrowsingService(mutableListOf(low, high), ratings(mapOf(1 to 2, 2 to 9)))
        val g = casualGroup()

        assertEquals(2, service.choose(g))
    }

    @Test
    fun browsingChooseBreaksRatingTieByLowestId() {
        val a = defaultData.copy(id = 5)
        val b = defaultData.copy(id = 3)
        val service = BrowsingService(mutableListOf(a, b), ratings(mapOf(5 to 4, 3 to 4)))
        val g = casualGroup()

        assertEquals(3, service.choose(g))
    }

    @Test
    fun browsingChooseExcludesRestaurantNotAcceptingNewCustomersThisTick() {
        // Every clock-touching test resets the evening first so it is self-contained
        // regardless of what earlier tests left GlobalClock at.
        GlobalClock.advanceEvening()
        repeat(22) { GlobalClock.advanceTick() } // tickInEvening = 22

        val closingSoon = defaultData.copy(id = 1, openingTick = 1, closingTick = 24) // closes accepting at 21
        val service = BrowsingService(mutableListOf(closingSoon), ratings(mapOf(1 to 5)))
        val g = casualGroup()

        assertNull(service.choose(g))
    }

    @Test
    fun browsingChooseForEventFiltersOutRestaurantsNotHostingEvents() {
        val noEvents = defaultData.copy(id = 1, hostsEvents = false)
        val service = BrowsingService(mutableListOf(noEvents), ratings(mapOf(1 to 5)))
        val g = eventGroupMock()

        assertNull(service.chooseForEvent(g, 20))
    }

    @Test
    fun browsingChooseForEventFiltersByRemainingEventSeats() {
        val fewSeats = defaultData.copy(id = 1, totalSeats = 10, eventSeatsBookedStatic = mapOf(20 to 5))
        val service = BrowsingService(mutableListOf(fewSeats), ratings(mapOf(1 to 5)))
        val g = eventGroupMock(size = 10) // only 5 left

        assertNull(service.chooseForEvent(g, 20))
    }
}
