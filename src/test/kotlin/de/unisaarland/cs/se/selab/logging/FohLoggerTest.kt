package de.unisaarland.cs.se.selab.logging

import java.io.PrintWriter
import java.io.StringWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class FohLoggerTest {

    private fun captureAt(level: LogLevel): StringWriter {
        val buffer = StringWriter()
        LogSink.configure(level, PrintWriter(buffer))
        return buffer
    }

    @Test
    fun noReservingMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.noReserving(restaurantId = 1, groupId = 3)
        assertEquals(
            "[IMPORTANT] FOH No Reserving (R 1): No table could be reserved for group 3.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun mergingTablesSortsOldIdsAscending() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Foh.mergingTables(restaurantId = 1, groupId = 3, oldIds = listOf(9, 2, 5), mergedId = 2)
        assertEquals(
            "[INFO] FOH Merging Tables (R 1): For group 3 the tables 2,5,9 were merged into 2.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun seatingMessageSortsWaiterIdsAscending() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.seating(restaurantId = 1, groupId = 3, tableId = 5, waiterIds = listOf(4, 2))
        assertEquals(
            "[IMPORTANT] FOH Seating (R 1): Group 3 seated at table 5 by waitstaff 2,4.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noSeatingNoWaiterMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Foh.noSeatingNoWaiter(restaurantId = 1, groupId = 3)
        assertEquals(
            "[INFO] FOH No Seating (R 1): No free waitstaff available for group 3.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noSeatingNoTableMessageAndLevel() {
        val buffer = captureAt(LogLevel.INFO)
        Logger.Foh.noSeatingNoTable(restaurantId = 1, waiterId = 4, groupId = 3)
        assertEquals(
            "[INFO] FOH No Seating (R 1): Assigned waitstaff 4 but no table available, group 3 is sent away.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun orderingInPersonIncludesWaitstaffClause() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.ordering(
            restaurantId = 1,
            groupId = 3,
            orderId = 9,
            dishes = mapOf("Pasta" to 2, "Salad" to 1),
            waiterId = 4,
        )
        assertEquals(
            "[IMPORTANT] FOH Ordering (R 1): Group 3 placed order 9 of Pasta:2,Salad:1 with waitstaff 4.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun orderingDeliveryOmitsWaitstaffClause() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.ordering(
            restaurantId = 1,
            groupId = 3,
            orderId = 9,
            dishes = mapOf("Pasta" to 2),
            waiterId = null,
        )
        assertEquals(
            "[IMPORTANT] FOH Ordering (R 1): Group 3 placed order 9 of Pasta:2.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noOrderingMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.noOrdering(restaurantId = 1, groupId = 3, customers = 2)
        assertEquals(
            "[IMPORTANT] FOH No Ordering (R 1): Group 3 could not place an order for 2 customers, " +
                "they leave the restaurant.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun seatingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Foh.seatingStatus(restaurantId = 1, waiters = 2, customers = 5, tables = 3)
        assertEquals(
            "[DEBUG] FOH Seating Status (R 1): 2 waitstaff seated 5 customers on 3 tables.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun orderingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Foh.orderingStatus(restaurantId = 1, customers = 5, waiters = 2)
        assertEquals(
            "[DEBUG] FOH Ordering Status (R 1): The restaurant received orders from 5 customers, 2 waitstaff " +
                "took orders.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun servingMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.serving(
            restaurantId = 1,
            waiterId = 4,
            dishes = mapOf("Pasta" to 2),
            tableId = 5,
            ticksAfterOrder = 3,
        )
        assertEquals(
            "[IMPORTANT] FOH Serving (R 1): Waitstaff 4 serves Pasta:2 to table 5 3 ticks after ordering.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun noServingMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Foh.noServing(restaurantId = 1, waiterId = 4, meals = 2, tableId = 5)
        assertEquals(
            "[DEBUG] FOH No Serving (R 1): Waitstaff 4 did not serve 2 meals to table 5.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun deliveryHandoverMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.deliveryHandover(
            restaurantId = 1,
            waiterId = 4,
            dishes = mapOf("Pasta" to 2),
            driverId = 6,
            orderId = 9,
        )
        assertEquals(
            "[IMPORTANT] FOH Delivery (R 1): Waitstaff 4 serves Pasta:2 meals to driver 6 for order 9.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun servingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Foh.servingStatus(restaurantId = 1, waiters = 2, meals = 5)
        assertEquals(
            "[DEBUG] FOH Serving Status (R 1): 2 waitstaff served 5 meals.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun escortingMessageAndLevel() {
        val buffer = captureAt(LogLevel.IMPORTANT)
        Logger.Foh.escorting(restaurantId = 1, waiterId = 4, customers = 2, groupId = 3, tableId = 5)
        assertEquals(
            "[IMPORTANT] FOH Escorting (R 1): Waitstaff 4 escorts 2 customers of group 3 from table 5 outside.",
            buffer.toString().trim(),
        )
    }

    @Test
    fun escortingStatusMessageAndLevel() {
        val buffer = captureAt(LogLevel.DEBUG)
        Logger.Foh.escortingStatus(restaurantId = 1, waiters = 2, customers = 5)
        assertEquals(
            "[DEBUG] FOH Escorting Status (R 1): 2 waitstaff escorted 5 customers this tick.",
            buffer.toString().trim(),
        )
    }
}
