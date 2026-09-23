package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.config.scenarioParser.CustomerGroupJsonDto
import de.unisaarland.cs.se.selab.config.scenarioParser.CustomerGroupSerialiser
import de.unisaarland.cs.se.selab.config.scenarioParser.FoodPreferenceDto
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/** Checks customer-group conversion and validation of references, preferences and scheduling. */
class CustomerGroupSerialiserTest {
    private val model = ParsedModel()
    private val serialiser = CustomerGroupSerialiser(model)
    private val rice = Ingredient("rice", UnitType.G, 20, 5)
    private val potato = Ingredient("potato", UnitType.G, 10, 3)

    init {
        check(model.registerIngredient(rice))
        check(model.registerIngredient(potato))
        check(model.registerRecipe(recipe(1, "Rice Bowl", rice, RestaurantType.ASIAN)))
        check(model.registerRecipe(recipe(2, "Potato Soup", potato, RestaurantType.EUROPEAN)))
        val restaurant = mock<Restaurant> {
            on { getId() } doReturn 7
            on { openingTick } doReturn 3
            on { closingTick } doReturn 20
        }
        check(model.registerRestaurant(restaurant))
    }

    @Test
    fun validRegularHasCorrectTypeScheduleRestaurantAndPreferences() {
        val group = assertIs<RegularCustomerGroup>(serialiser.serialise(regular()))

        assertEquals(11, group.id())
        assertEquals(GroupType.REGULAR, group.groupType())
        assertEquals(3, group.groupSize())
        assertEquals(TableType.BAR, group.tableType())
        assertEquals(5, group.visitingTick())
        assertEquals(7, group.homeRestaurant())
        assertEquals(2, group.visitingStart())
        assertEquals(3, group.visitingPeriod())
        assertTrue(group.visitsOn(2))
        assertTrue(group.visitsOn(5))
        assertFalse(group.visitsOn(3))
        assertFalse(group.isDelivery())
        assertEquals(3, group.members().size)
        val preference = group.preferences().single()
        assertEquals(2, preference.size())
        assertEquals(setOf(potato), preference.excluded())
        assertEquals(setOf(rice), preference.preferred())
        assertEquals(listOf("Rice Bowl"), preference.favouriteDishNames())
        assertEquals(preference.excluded(), group.members()[0].preference()?.excluded())
        assertEquals(preference.preferred(), group.members()[0].preference()?.preferred())
        assertEquals(preference.excluded(), group.members()[1].preference()?.excluded())
        assertEquals(preference.preferred(), group.members()[1].preference()?.preferred())
        assertNull(group.members()[2].preference())
    }

    @Test
    fun validCasualDeliveryHasCorrectValuesAndAcceptsOrderTickOne() {
        // ceil(6 / 5) = 2 travel ticks; 6 - 2 - 3 = 1 is a valid order tick.
        val group = assertIs<CasualCustomerGroup>(serialiser.serialise(casual()))

        assertEquals(12, group.id())
        assertEquals(GroupType.CASUAL, group.groupType())
        assertEquals(2, group.groupSize())
        assertEquals(2, group.members().size)
        assertEquals(TableType.COMMON, group.tableType())
        assertEquals(6, group.visitingTick())
        assertEquals(setOf(RestaurantType.ASIAN, RestaurantType.EUROPEAN), group.restaurantTypes())
        assertEquals(listOf(1, 3), group.visitingEvenings())
        assertTrue(group.visitsOn(3))
        assertFalse(group.visitsOn(2))
        assertTrue(group.isDelivery())
        assertEquals(6, group.getDeliveryDistance())
        assertEquals(RatingLikelihood.SOME, group.ratingLikelihood())
        assertNull(group.homeRestaurant())
        assertTrue(group.preferences().isEmpty())
        assertTrue(group.members().all { it.preference() == null })
    }

    @Test
    fun validEventHasCorrectTypeBookingEveningAndFavouriteDishes() {
        val group = assertIs<EventCustomerGroup>(serialiser.serialise(event()))

        assertEquals(13, group.id())
        assertEquals(GroupType.EVENT, group.groupType())
        assertEquals(4, group.groupSize())
        assertEquals(4, group.members().size)
        assertEquals(TableType.SEPARATED, group.tableType())
        assertEquals(8, group.visitingTick())
        assertEquals(4, group.getEventEvening())
        assertTrue(group.booksOn(1))
        assertFalse(group.booksOn(2))
        assertEquals(setOf(RestaurantType.ASIAN, RestaurantType.EUROPEAN), group.restaurantTypes())
        assertEquals(
            mapOf(RestaurantType.ASIAN to "Rice Bowl", RestaurantType.EUROPEAN to "Potato Soup"),
            group.favouriteDishes(),
        )
        assertNull(group.homeRestaurant())
        assertFalse(group.isDelivery())
    }

    @Test
    fun sumOfPreferenceSizesExceedingGroupSizeIsRejected() {
        val preferences = mutableListOf(
            FoodPreferenceDto(size = 2, preferredIngredients = mutableListOf("rice")),
            FoodPreferenceDto(size = 2, excludedIngredients = mutableListOf("potato")),
        )
        assertNull(serialiser.serialise(regular().copy(foodPreferences = preferences)))
    }

    @Test
    fun unknownExcludedIngredientIsRejected() {
        assertInvalidPreference(FoodPreferenceDto(size = 1, excludedIngredients = mutableListOf("unknown")))
    }

    @Test
    fun unknownPreferredIngredientIsRejected() {
        assertInvalidPreference(FoodPreferenceDto(size = 1, preferredIngredients = mutableListOf("unknown")))
    }

    @Test
    fun unknownFavouriteDishIsRejected() {
        assertInvalidPreference(FoodPreferenceDto(size = 1, favoriteDishes = mutableListOf("Unknown Dish")))
    }

    @Test
    fun regularWithUnknownRestaurantIsRejected() {
        assertNull(serialiser.serialise(regular().copy(restaurant = 999)))
    }

    @Test
    fun regularVisitingBeforeRestaurantOpensIsRejected() {
        assertNull(serialiser.serialise(regular().copy(visitingTick = 2)))
    }

    @Test
    fun regularVisitingInLastThreeOpeningTicksIsRejected() {
        assertNull(serialiser.serialise(regular().copy(visitingTick = 18)))
    }

    @Test
    fun deliveryWithOrderTickBeforeOneIsRejected() {
        // ceil(6 / 5) = 2; 5 - 2 - 3 = 0, before the serving phase.
        assertNull(serialiser.serialise(casual().copy(visitingTick = 5)))
    }

    @Test
    fun eventWithoutFavouriteDishesIsRejected() {
        assertNull(serialiser.serialise(event().copy(favoriteDishes = null)))
    }

    @Test
    fun eventMissingFavouriteForOneRequestedRestaurantTypeIsRejected() {
        assertNull(serialiser.serialise(event().copy(favoriteDishes = mapOf("ASIAN" to "Rice Bowl"))))
    }

    @Test
    fun eventWithUnknownFavouriteDishIsRejected() {
        val favourites = mapOf("ASIAN" to "Unknown Dish", "EUROPEAN" to "Potato Soup")
        assertNull(serialiser.serialise(event().copy(favoriteDishes = favourites)))
    }

    @Test
    fun eventWithFavouriteFromAnotherRestaurantTypeIsAccepted() {
        // Forum 352 (staff): the favourite only has to exist as a dish, not be a basic dish of
        // that restaurant type.
        val favourites = mapOf("ASIAN" to "Potato Soup", "EUROPEAN" to "Potato Soup")
        assertNotNull(serialiser.serialise(event().copy(favoriteDishes = favourites)))
    }

    private fun assertInvalidPreference(preference: FoodPreferenceDto) {
        assertNull(serialiser.serialise(regular().copy(foodPreferences = mutableListOf(preference))))
    }

    @Test
    fun ingredientCannotBeBothExcludedAndPreferred() {
        val preference = FoodPreferenceDto(
            size = 1,
            excludedIngredients = mutableListOf("rice"),
            preferredIngredients = mutableListOf("rice"),
        )

        assertNull(
            serialiser.serialise(
                regular().copy(foodPreferences = mutableListOf(preference)),
            ),
        )
    }

    private fun regular() = CustomerGroupJsonDto(
        id = 11,
        type = "REGULAR",
        size = 3,
        visitingTick = 5,
        foodPreferences = mutableListOf(
            FoodPreferenceDto(
                size = 2,
                excludedIngredients = mutableListOf("potato"),
                preferredIngredients = mutableListOf("rice"),
                favoriteDishes = mutableListOf("Rice Bowl"),
            )
        ),
        tableType = "BAR",
        visitingStart = 2,
        visitingPeriod = 3,
        restaurant = 7,
    )

    private fun casual() = CustomerGroupJsonDto(
        id = 12,
        type = "CASUAL",
        size = 2,
        visitingTick = 6,
        foodPreferences = mutableListOf(),
        visitingEvenings = mutableListOf(1, 3),
        deliveryDistance = 6,
        ratingLikelihood = "SOME",
        restaurantTypes = mutableListOf("ASIAN", "EUROPEAN"),
    )

    private fun event() = CustomerGroupJsonDto(
        id = 13,
        type = "EVENT",
        size = 4,
        visitingTick = 8,
        foodPreferences = mutableListOf(),
        tableType = "SEPARATED",
        restaurantTypes = mutableListOf("ASIAN", "EUROPEAN"),
        eventEvening = 4,
        favoriteDishes = mapOf("ASIAN" to "Rice Bowl", "EUROPEAN" to "Potato Soup"),
    )

    private fun recipe(id: Int, name: String, ingredient: Ingredient, type: RestaurantType) = Recipe(
        id = id,
        dishName = name,
        minuteDuration = 10,
        cookTypes = setOf(CookType.EXEC),
        ingredients = mutableListOf(RecipeIngredient(ingredient, 1)),
        basicDishFor = type,
    )
}
