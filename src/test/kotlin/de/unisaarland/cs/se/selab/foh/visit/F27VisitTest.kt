package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.GroupType
import de.unisaarland.cs.se.selab.simulation.ratings.Experience
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Tests Visit's hold-back window for serving and the experience()/rating outcome.
 * Covers F27, no waiter/table assignment logic.
 */
class F27VisitTest {

    private lateinit var group: CustomerGroup
    private lateinit var customer: Customer
    private lateinit var customer2: Customer
    private lateinit var recipe: Recipe
    private lateinit var visit: Visit

    @BeforeEach
    fun setUp() {
        customer = mock()
        customer2 = mock()
        whenever(customer.status()).thenReturn(CustomerStatus.ORDERED)
        whenever(customer2.status()).thenReturn(CustomerStatus.ORDERED)

        group = mock()
        whenever(group.members()).thenReturn(mutableListOf(customer))
        whenever(group.groupType()).thenReturn(GroupType.CASUAL)
        whenever(group.id()).thenReturn(1)

        recipe = Recipe(1, "Soup", 10, setOf(CookType.EXEC), mutableListOf(), null)
        visit = Visit(group)
    }

    @Test
    fun nothingIsServableBeforeAnyMealOfTheOrderHasBeenCooked() {
        val meal = Meal(1, customer, recipe, status = MealStatus.QUEUED)
        visit.order = Order(group, 1, 1, 1, isDelivery = false, meals = mutableListOf(meal))
        // firstMealTick still null
        assertTrue(visit.servableMeals(tick = 1).isEmpty())
    }

    @Test
    fun fullyCookedOrderServedImmediately() {
        val meal = Meal(1, customer, recipe, status = MealStatus.COOKED)
        visit.order = Order(group, 1, 1, 1, isDelivery = false, meals = mutableListOf(meal))
        visit.firstMealTick = 5
        assertEquals(1, visit.servableMeals(tick = 5).size)
    }

    @Test
    fun groupNeverOrderedHasNegativeExperience() {
        assertEquals(Experience.NEGATIVE, visit.experience())
    }

    @Test
    fun groupMissingAMeaHasNegativeExperience() {
        visit.orderedTick = 1
        whenever(customer.servedTick()).thenReturn(null)
        assertEquals(Experience.NEGATIVE, visit.experience())
    }

    @Test
    fun foodArrivingInside4TickWindowHasPositive() {
        visit.orderedTick = 1
        whenever(customer.servedTick()).thenReturn(4) // waited = 3
        assertEquals(Experience.POSITIVE, visit.experience())
    }

    @Test
    fun foodArrivingExactlyAt4TickNeutral() {
        visit.orderedTick = 1
        whenever(customer.servedTick()).thenReturn(5) // waited = 4
        assertEquals(Experience.NEUTRAL, visit.experience())
    }

    @Test
    fun foodArrivingAfterDeadlineNegative() {
        visit.orderedTick = 1
        whenever(customer.servedTick()).thenReturn(6) // waited = 5
        assertEquals(Experience.NEGATIVE, visit.experience())
    }
}
