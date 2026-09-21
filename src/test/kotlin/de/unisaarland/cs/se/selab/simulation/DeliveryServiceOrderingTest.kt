package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.DeliveryPreference
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.captureLog
import de.unisaarland.cs.se.selab.testsupport.Fixtures.logLines
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * F24, F29: a delivery group orders straight at the restaurant it decided on, by the same dish rules as
 * a group at a table (specification adjustment #12), and the delivery service finds a free driver of that
 * restaurant for the finished order.
 *
 * The restaurant parts are mocked so that the menu offers exactly the dishes each test names; the
 * restaurant ids 951+ are used by no other test, because the delivery service is a singleton.
 */
class DeliveryServiceOrderingTest {

    private val rice = Ingredient("rice", UnitType.G, 10, 5)
    private val tofu = Ingredient("tofu", UnitType.X, 10, 5)
    private val riceBowl = recipe(1, "Rice Bowl", rice)
    private val tofuBowl = recipe(2, "Tofu Bowl", tofu)
    private val tofuRice = recipe(3, "Tofu Rice", tofu, rice)

    private val menu = mock<Menu>()
    private val kitchen = mock<Kitchen>()
    private val desk = mock<DeliveryDesk>()
    private val restaurant = restaurant(RESTAURANT)

    private fun recipe(id: Int, name: String, vararg ingredients: Ingredient) = Recipe(
        id,
        name,
        DURATION,
        setOf(CookType.EXEC),
        ingredients.map { RecipeIngredient(it, 1) }.toMutableList(),
        null,
    )

    private fun restaurant(id: Int): Restaurant {
        val foh = mock<FrontOfTheHouse>()
        whenever(foh.getDeliveryDesk()).thenReturn(desk)
        val restaurant = mock<Restaurant>()
        whenever(restaurant.getId()).thenReturn(id)
        whenever(restaurant.getMenu()).thenReturn(menu)
        whenever(restaurant.getPantry()).thenReturn(mock<Pantry>())
        whenever(restaurant.getKitchen()).thenReturn(kitchen)
        whenever(restaurant.getFoh()).thenReturn(foh)
        return restaurant
    }

    private fun preference(
        excluded: Set<Ingredient> = emptySet(),
        preferred: Set<Ingredient> = emptySet(),
        favourites: List<String> = emptyList(),
    ) = FoodPreference(1, excluded, preferred, favourites)

    /** A delivery group of 7 km whose members follow [preferences] in the given order. */
    private fun deliveryGroup(vararg preferences: FoodPreference?) = CasualCustomerGroup(
        GROUP,
        preferences.size,
        TableType.COMMON,
        VISITING_TICK,
        preferences.map { Customer(it) },
        preferences.filterNotNull(),
        setOf(RestaurantType.ASIAN),
        listOf(1),
        DeliveryPreference(DISTANCE, RatingLikelihood.ALWAYS),
    )

    private fun offer(vararg perCustomer: List<Recipe>) {
        val first = perCustomer.first()
        val rest = perCustomer.drop(1).toTypedArray()
        whenever(menu.getOrderables()).thenReturn(first, *rest)
    }

    @Test
    fun customerWithoutPreferencesOrdersTheDishWithTheHighestId() {
        offer(listOf(riceBowl, tofuBowl, tofuRice))
        val log = captureLog(LogLevel.IMPORTANT)

        val order = assertNotNull(DeliveryService.placeOrder(deliveryGroup(null), restaurant))

        assertEquals(mapOf("Tofu Rice" to 1), order.dishCounts())
        assertEquals(
            listOf(
                "[IMPORTANT] FOH Ordering (R $RESTAURANT): Group $GROUP placed order ${order.getId()} of Tofu Rice:1.",
            ),
            logLines(log),
        )
    }

    @Test
    fun theOrderGoesToTheKitchenAndTheDeliveryDesk() {
        offer(listOf(riceBowl))

        val order = assertNotNull(DeliveryService.placeOrder(deliveryGroup(null), restaurant))

        verify(kitchen).enqueue(order)
        verify(desk).enqueue(order)
        assertEquals(true, order.getIsDelivery())
        assertEquals(RESTAURANT, order.getRestaurantId())
        assertEquals(listOf(order.getId()), order.getMeals().map { it.orderId })
    }

    @Test
    fun anExcludedIngredientRulesTheDishOut() {
        offer(listOf(riceBowl, tofuBowl, tofuRice))

        val order = DeliveryService.placeOrder(deliveryGroup(preference(excluded = setOf(tofu))), restaurant)

        assertEquals(mapOf("Rice Bowl" to 1), assertNotNull(order).dishCounts())
    }

    @Test
    fun aFavouriteDishBeatsTheHighestId() {
        offer(listOf(riceBowl, tofuBowl, tofuRice))

        val group = deliveryGroup(preference(favourites = listOf("Tofu Bowl")))

        val order = DeliveryService.placeOrder(group, restaurant)

        assertEquals(mapOf("Tofu Bowl" to 1), assertNotNull(order).dishCounts())
    }

    @Test
    fun moreFavouritesAreTriedInTheirDeclaredOrder() {
        offer(listOf(riceBowl, tofuRice))

        val favourites = listOf("Tofu Bowl", "Rice Bowl", "Tofu Rice")
        val order = DeliveryService.placeOrder(deliveryGroup(preference(favourites = favourites)), restaurant)

        // Tofu Bowl is not offered, so the next favourite in the declared order is taken.
        assertEquals(mapOf("Rice Bowl" to 1), assertNotNull(order).dishCounts())
    }

    @Test
    fun theMostPreferredIngredientsBeatTheHighestId() {
        offer(listOf(riceBowl, tofuBowl))

        val order = DeliveryService.placeOrder(deliveryGroup(preference(preferred = setOf(rice))), restaurant)

        assertEquals(mapOf("Rice Bowl" to 1), assertNotNull(order).dishCounts())
    }

    @Test
    fun amongEquallyPreferredDishesTheHighestIdWins() {
        offer(listOf(riceBowl, tofuBowl, tofuRice))

        val order = DeliveryService.placeOrder(deliveryGroup(preference(preferred = setOf(rice))), restaurant)

        // Rice Bowl and Tofu Rice both contain rice once.
        assertEquals(mapOf("Tofu Rice" to 1), assertNotNull(order).dishCounts())
    }

    @Test
    fun customersWithTheMostExcludedIngredientsChooseFirst() {
        // The first chooser sees Rice Bowl and Tofu Rice, the second chooser only Tofu Rice.
        offer(listOf(riceBowl, tofuRice), listOf(tofuRice))
        val log = captureLog(LogLevel.IMPORTANT)
        val relaxed = null
        val picky = preference(excluded = setOf(tofu))

        // The relaxed customer is listed first, but the picky one has more exclusions and chooses first.
        val order = DeliveryService.placeOrder(deliveryGroup(relaxed, picky), restaurant)

        // Had the relaxed customer chosen first, it would have taken Tofu Rice (highest id) and the
        // picky one would have found nothing.
        assertEquals(mapOf("Rice Bowl" to 1, "Tofu Rice" to 1), assertNotNull(order).dishCounts())
        assertEquals(1, logLines(log).size)
    }

    @Test
    fun customersWhoFindNoDishAreLoggedAndTheOthersStillOrder() {
        offer(listOf(riceBowl), emptyList())
        val log = captureLog(LogLevel.IMPORTANT)

        val order = assertNotNull(DeliveryService.placeOrder(deliveryGroup(null, null), restaurant))

        assertEquals(mapOf("Rice Bowl" to 1), order.dishCounts())
        assertEquals(
            "[IMPORTANT] FOH No Ordering (R $RESTAURANT): Group $GROUP could not place an order for 1 customers, " +
                "they leave the restaurant.",
            logLines(log).last(),
        )
    }

    @Test
    fun noOrderIsPlacedWhenNobodyFindsADish() {
        offer(listOf(tofuBowl))
        val log = captureLog(LogLevel.IMPORTANT)
        val picky = preference(excluded = setOf(tofu))

        val order = DeliveryService.placeOrder(deliveryGroup(picky, picky), restaurant)

        assertNull(order)
        verify(kitchen, never()).enqueue(any())
        verify(desk, never()).enqueue(any())
        assertEquals(
            listOf(
                "[IMPORTANT] FOH No Ordering (R $RESTAURANT): Group $GROUP could not place an order for 2 customers, " +
                    "they leave the restaurant.",
            ),
            logLines(log),
        )
    }

    @Test
    fun noDriverIsChosenForARestaurantWithoutRegisteredDrivers() {
        val realDesk = DeliveryDesk(mutableListOf(), EMPTY_RESTAURANT)
        val owner = mock<Restaurant>()
        val foh = mock<FrontOfTheHouse>()
        whenever(owner.getId()).thenReturn(EMPTY_RESTAURANT)
        whenever(owner.getFoh()).thenReturn(foh)
        whenever(foh.getDeliveryDesk()).thenReturn(realDesk)
        DeliveryService.setSimulator(
            Simulator(
                0,
                mutableListOf(owner),
                CustomerRegistry(mutableListOf()),
                mutableListOf(),
                BrowsingService(mutableListOf(), RatingBook),
            ),
        )

        assertNull(DeliveryService.chooseDriverForOrder(EMPTY_RESTAURANT))
    }

    @Test
    fun anAddedDriverGetsTheFirstIdOfItsRestaurantAndKeepsIt() {
        val realDesk = DeliveryDesk(mutableListOf(), DRIVER_RESTAURANT)
        val owner = mock<Restaurant>()
        val foh = mock<FrontOfTheHouse>()
        whenever(owner.getId()).thenReturn(DRIVER_RESTAURANT)
        whenever(owner.getFoh()).thenReturn(foh)
        whenever(foh.getDeliveryDesk()).thenReturn(realDesk)
        RatingBook.initializeRatings(DRIVER_RESTAURANT, 0, 0)
        DeliveryService.setSimulator(
            Simulator(
                0,
                mutableListOf(owner),
                CustomerRegistry(mutableListOf()),
                mutableListOf(),
                BrowsingService(mutableListOf(), RatingBook),
            ),
        )

        DeliveryService.addDriver(DRIVER_RESTAURANT)

        assertEquals(1, realDesk.getDrivers().size)
        assertEquals(1, DeliveryService.chooseDriverForOrder(DRIVER_RESTAURANT))
        // The driver is still free, so asking again returns the same driver with the same id.
        assertEquals(1, DeliveryService.chooseDriverForOrder(DRIVER_RESTAURANT))

        DeliveryService.rmDriver(DRIVER_RESTAURANT)

        assertEquals(0, realDesk.getDrivers().size)
        assertNull(DeliveryService.chooseDriverForOrder(DRIVER_RESTAURANT))
    }

    /** Values shared by the tests. */
    private companion object {
        const val RESTAURANT = 951
        const val EMPTY_RESTAURANT = 952
        const val DRIVER_RESTAURANT = 953
        const val GROUP = 7
        const val DURATION = 10
        const val DISTANCE = 7
        const val VISITING_TICK = 10
    }
}
