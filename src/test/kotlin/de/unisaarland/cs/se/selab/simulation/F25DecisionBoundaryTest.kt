package de.unisaarland.cs.se.selab.simulation

import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.DeliveryPreference
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import de.unisaarland.cs.se.selab.testsupport.Fixtures.event
import de.unisaarland.cs.se.selab.testsupport.Fixtures.recipe
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** F25: eligibility applies to every subgroup, and future events use their own time and capacity. */
class F25DecisionBoundaryTest {
    private val rice = Ingredient("rice", UnitType.G, 10, 2)
    private val fish = Ingredient("fish", UnitType.G, 10, 2)
    private val riceDish = recipe(1, listOf(RecipeIngredient(rice, 1)))
    private val fishDish = recipe(2, listOf(RecipeIngredient(fish, 1)))

    @BeforeTest
    fun freshClockAndRatings() {
        GlobalClock.advanceEvening()
        GlobalClock.advanceTick()
        RatingBook.initializeRatings(1, 10, 0)
        RatingBook.initializeRatings(2, 0, 0)
    }

    private fun restaurant(id: Int, dishes: List<Recipe>, opening: Int = 1, booked: Map<Int, Int> = emptyMap()) =
        RestaurantData(
            id,
            RestaurantType.ASIAN,
            opening,
            24,
            dishes,
            mapOf(TableType.COMMON to 4),
            0,
            true,
            4,
            booked,
        )

    private fun mixedGroup(): CasualCustomerGroup {
        val noFish = FoodPreference(1, setOf(fish), emptySet(), emptyList())
        val noRice = FoodPreference(1, setOf(rice), emptySet(), emptyList())
        return CasualCustomerGroup(
            1,
            2,
            TableType.COMMON,
            1,
            listOf(Customer(noFish), Customer(noRice)),
            listOf(noFish, noRice),
            setOf(RestaurantType.ASIAN),
            listOf(1),
            DeliveryPreference(0, RatingLikelihood.ALWAYS),
        )
    }

    @Test
    fun oneMemberWithoutAnEdibleDishExcludesTheHigherRatedRestaurant() {
        val service = BrowsingService(
            mutableListOf(restaurant(1, listOf(riceDish)), restaurant(2, listOf(riceDish, fishDish))),
            RatingBook,
        )
        assertEquals(2, service.choose(mixedGroup()))
    }

    @Test
    fun membersMayEachFindADifferentDishWithoutOneDishEveryoneCanEat() {
        val service = BrowsingService(mutableListOf(restaurant(1, listOf(riceDish, fishDish))), RatingBook)
        assertEquals(1, service.choose(mixedGroup()))
    }

    @Test
    fun eventUsesVisitingTimeAndItsOwnEveningsReservationsInsteadOfCurrentAvailability() {
        val service = BrowsingService(
            mutableListOf(
                restaurant(1, listOf(riceDish), opening = 8),
                restaurant(2, listOf(riceDish), opening = 4, booked = mapOf(4 to 4)),
            ),
            RatingBook,
        )
        val group = event(1, 4, eventEvening = 5, visitingTick = 5, types = setOf(RestaurantType.ASIAN))
        assertEquals(2, service.chooseForEvent(group, 5))
        assertEquals(0, service.entries[1].eventSeatsLeft(5))
    }
}
