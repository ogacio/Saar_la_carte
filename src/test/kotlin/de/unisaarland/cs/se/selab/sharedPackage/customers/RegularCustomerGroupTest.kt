package de.unisaarland.cs.se.selab.sharedPackage.customers
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import de.unisaarland.cs.se.selab.simulation.ratings.Rating
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** F22: RegularCustomerGroup: periodic visiting, rating, and the two-consecutive-failures rule. */

class RegularCustomerGroupTest {

    private fun group(
        id: Int = 1,
        groupSize: Int = 2,
        visitingStart: Int = 3,
        visitingPeriod: Int = 2,
        restaurantId: Int = 7,
    ) = RegularCustomerGroup(
        id = id,
        groupSize = groupSize,
        tableType = TableType.COMMON,
        visitingTick = 1,
        members = List(groupSize) { Customer(null) },
        preferences = emptyList(),
        visitingStart = visitingStart,
        visitingPeriod = visitingPeriod,
        restaurantId = restaurantId,
    )

    private fun recipe(id: Int) = Recipe(id, "dish$id", 10, setOf(CookType.EXEC), mutableListOf(), null)

    private fun visitWith(g: RegularCustomerGroup, evening: Int, recipes: List<Recipe>) {
        val meals = recipes.map { Meal(null, g.members()[0], it) }.toMutableList()
        val order = Order(g, g.homeRestaurant(), g.id(), 1, false, meals)
        g.recordVisit(evening, order)
    }

    /** Visits start on visitingStart, then every visitingPeriod evenings after. */
    @Test
    fun visitsOnStartAndEveryPeriodAfter() {
        val g = group(visitingStart = 3, visitingPeriod = 2)

        assertFalse(g.visitsOn(1))
        assertFalse(g.visitsOn(2))
        assertTrue(g.visitsOn(3))
        assertFalse(g.visitsOn(4))
        assertTrue(g.visitsOn(5))
        assertFalse(g.visitsOn(6))
        assertTrue(g.visitsOn(7))
    }

    /** A group that has given up no longer visits, even on an evening that fits its schedule. */
    @Test
    fun givenUpGroupDoesNotVisitOnScheduledEvening() {
        val g = group(visitingStart = 3, visitingPeriod = 2)
        g.recordOutcome(Experience.NEGATIVE)
        g.recordOutcome(Experience.NEGATIVE)

        assertTrue(g.hasGivenUp())
        assertFalse(g.visitsOn(5))
    }

    /** A negative experience gives a negative rating; positive and neutral both give positive. */
    @Test
    fun ratesNegativeExperienceNegativelyAndEverythingElsePositively() {
        val g = group()

        assertEquals(Rating.NEGATIVE, g.ratingFor(Experience.NEGATIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.POSITIVE))
        assertEquals(Rating.POSITIVE, g.ratingFor(Experience.NEUTRAL))
    }

    /** One failed attempt does not give up; failedAttempts reflects the running count. */
    @Test
    fun oneFailedAttemptDoesNotGiveUp() {
        val g = group()

        g.recordOutcome(Experience.NEGATIVE)

        assertEquals(1, g.failedAttempts())
        assertFalse(g.hasGivenUp())
    }

    /** Two consecutive failed attempts give up. */
    @Test
    fun twoConsecutiveFailedAttemptsGiveUp() {
        val g = group()

        g.recordOutcome(Experience.NEGATIVE)
        g.recordOutcome(Experience.NEGATIVE)

        assertEquals(2, g.failedAttempts())
        assertTrue(g.hasGivenUp())
    }

    /** A successful visit between two failures resets the streak, so it takes two more to give up. */
    @Test
    fun successfulVisitResetsTheFailureStreak() {
        val g = group()

        g.recordOutcome(Experience.NEGATIVE)
        g.recordOutcome(Experience.POSITIVE)
        g.recordOutcome(Experience.NEGATIVE)

        assertEquals(1, g.failedAttempts())
        assertFalse(g.hasGivenUp())
    }

    /** A neutral experience also counts as success and resets the streak, not just positive. */
    @Test
    fun neutralExperienceAlsoResetsTheFailureStreak() {
        val g = group()

        g.recordOutcome(Experience.NEGATIVE)
        g.recordOutcome(Experience.NEUTRAL)

        assertEquals(0, g.failedAttempts())
        assertFalse(g.hasGivenUp())
    }

    /** REGULAR groups are bound to one fixed restaurant, browse for none, and never deliver. */
    @Test
    fun isBoundToItsHomeRestaurantAndNeverBrowsesOrDelivers() {
        val g = group(restaurantId = 42)

        assertEquals(42, g.homeRestaurant())
        assertEquals(42, g.restaurantId())
        assertTrue(g.restaurantTypes().isEmpty())
        assertEquals(0, g.getEventEvening())
        assertEquals(0, g.getDeliveryDistance())
        assertFalse(g.isDelivery())
    }

    /** Expected dishes count each recipe once per meal it appeared in, across the last visits. */
    @Test
    fun expectedDishesCountsRecipesFromPastVisits() {
        val g = group()
        val chickenRice = recipe(1)
        val beefPasta = recipe(2)
        visitWith(g, evening = 1, recipes = listOf(chickenRice, chickenRice, beefPasta))

        val expected = g.expectedDishes(listOf(chickenRice, beefPasta))

        assertEquals(2, expected[chickenRice])
        assertEquals(1, expected[beefPasta])
    }

    /** A dish from a past visit that is no longer on the restaurant's menu is not expected. */
    @Test
    fun expectedDishesIgnoresDishesNoLongerOnTheMenu() {
        val g = group()
        val discontinued = recipe(1)
        val stillOnMenu = recipe(2)
        visitWith(g, evening = 1, recipes = listOf(discontinued, stillOnMenu))

        val expected = g.expectedDishes(listOf(stillOnMenu))

        assertFalse(expected.containsKey(discontinued))
        assertEquals(1, expected[stillOnMenu])
    }
}
