package de.unisaarland.cs.se.selab.foh

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.DeliveryPreference
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Tests DeliveryDriver's outbound trip and the accepted-vs-rejected branch at arrival, driven
 * through a real CasualCustomerGroup's hasGivenUp(). Covers F27, no driver assignment/desk logic.
 *
 * NOTE: Logger.configure(...) below assumes the signature/package shown in the class diagram.
 * If DeliveryDriver's Logger calls throw before this line runs, adjust to match the real signature.
 */
class F27DeliveryDriverTest {

    @BeforeEach
    fun setUp() {
        GlobalClock.advanceEvening() // isolates tickInEvening from earlier tests
        Logger.configure(LogLevel.DEBUG, PrintWriter(StringWriter()))
    }

    private fun buildOrder(group: CasualCustomerGroup, tick: Int): Order {
        val recipe = Recipe(1, "Soup", 10, setOf(CookType.EXEC), mutableListOf(), null)
        val meal = Meal(null, Customer(null), recipe)
        val order = Order(group, 1, group.id(), tick, isDelivery = true, meals = mutableListOf(meal))
        meal.orderId = order.getId()
        return order
    }

    @Test
    fun deliveryDriverServesGroupThatHasNotGivenUp() {
        GlobalClock.advanceTick() // tick = 1
        val group = CasualCustomerGroup(
            1, 1, TableType.COMMON, GlobalClock.getTickInEvening(), listOf(Customer(null)), emptyList(),
            setOf(RestaurantType.EUROPEAN), listOf(1), DeliveryPreference(1, RatingLikelihood.NEVER),
        )
        val order = buildOrder(group, GlobalClock.getTickInEvening())
        val driver = DeliveryDriver(restaurantId = 1)
        driver.setId(1)
        driver.receiveOrder(order) // distance 1 -> travelTicks = (1 + 4) / 5 = 1

        GlobalClock.advanceTick() // tick = 2, ticksLeft reaches 0 on this drive()
        driver.drive()
        driver.arrive()
        driver.deliverAccepted()

        val resolved = driver.takeResolvedOrder()
        assertEquals(order, resolved?.first)
        assertEquals(false, resolved?.second)
        assertTrue(driver.isReturning())
        assertEquals(GlobalClock.getTickInEvening(), order.getMeals().first().customer.servedTick())
    }

    @Test
    fun deliveryDriverFailsWhenGroupHasGivenUpBeforeArrival() {
        GlobalClock.advanceTick() // tick = 1, visitingTick = 1
        val group = CasualCustomerGroup(
            1, 1, TableType.COMMON, 1, listOf(Customer(null)), emptyList(),
            setOf(RestaurantType.EUROPEAN), listOf(1), DeliveryPreference(21, RatingLikelihood.NEVER),
        )
        val order = buildOrder(group, 1)
        // DeliveryService.placeOrder does this in the real flow; without it deliveryOrderPending
        // stays false and the group can never become due to give up.
        group.orderPlaced()
        val driver = DeliveryDriver(restaurantId = 1)
        driver.setId(1)
        driver.receiveOrder(order) // distance 21 -> travelTicks = (21 + 4) / 5 = 5

        // 5 outbound ticks land on tick = 6; the group's deadline was visitingTick(1) + 3 = 4.
        repeat(5) {
            GlobalClock.advanceTick()
            driver.drive()
        }
        // FrontOfTheHouse.dropGivenUpOrders records this in the deadline tick; the driver reacts to
        // the recorded give-up, not to the deadline, so a delivery landing in that tick still counts.
        group.markDeliveryGivenUp()
        driver.arrive()
        driver.deliverRejected()

        val resolved = driver.takeResolvedOrder()
        assertEquals(order, resolved?.first)
        assertEquals(true, resolved?.second)
        assertTrue(driver.isReturning())
    }
}
