package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class KitchenLoggerTest {

    private fun captureAt(level: LogLevel): StringWriter {
        val buffer = StringWriter()
        LogSink.configure(level, PrintWriter(buffer))
        return buffer
    }

    @Test
    fun pantryRemovedMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Kitchen.pantryRemoved(restaurantId = 1, amount = 250, unit = UnitType.G, name = "rice")
        assertEquals(
            "[DEBUG] Pantry (R 1): Removed 250 g of rice from the pantry.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun procuredMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Kitchen.procured(restaurantId = 1, amount = 500, unit = UnitType.ML, name = "oil")
        assertEquals(
            "[DEBUG] Pantry (R 1): Procured 500 mL of oil from the supplier.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun restockedMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Kitchen.restocked(restaurantId = 2)
        assertEquals(
            "[INFO] Pantry (R 2): Restocked ingredients.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun dishAssignmentMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Kitchen.dishAssignment(
            restaurantId = 1,
            cookId = 3,
            cookType = CookType.EXEC,
            meals = 2,
            dishName = "Rice Bowl",
            sourceOrderId = 5,
            allOrderIds = listOf(5, 6),
        )
        assertEquals(
            "[IMPORTANT] Kitchen Dish Assignment (R 1): Cook 3 of type EXEC starts cooking 2 meals of dish " +
                "Rice Bowl based on order 5 for orders 5,6.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun mealCookedMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Kitchen.mealCooked(restaurantId = 1, cookId = 3, meals = 2, dishName = "Rice Bowl", ticksAfterOrder = 4)
        assertEquals(
            "[IMPORTANT] Kitchen Meal Cooked (R 1): Cook 3 finished cooking 2 meals of dish Rice Bowl " +
                "4 ticks after ordering.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun kitchenStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Kitchen.kitchenStatus(restaurantId = 1, cooks = 2, total = 4, finished = 1, servable = 3)
        assertEquals(
            "[DEBUG] Kitchen Status (R 1): 2 cooks were active cooking 4 and finishing 1 meals. " +
                "3 meals can be served by the waitstaff.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun logsBelowTheConfiguredLevelAreSuppressed() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Kitchen.pantryRemoved(restaurantId = 1, amount = 1, unit = UnitType.G, name = "rice")
        assertEquals("", buffer.toString().trim())
    }
}
