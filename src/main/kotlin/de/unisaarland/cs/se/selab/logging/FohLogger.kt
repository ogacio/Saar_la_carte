package de.unisaarland.cs.se.selab.logging

/**
 * The statements of the front of the house: reserving, seating, ordering, serving and escorting.
 */
object FohLogger {
    private fun tag(restaurantId: Int) = LogFormat.restaurantTag(restaurantId)

    /**
     * Reports that no table could be reserved for group [groupId].
     */
    fun noReserving(restaurantId: Int, groupId: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "FOH No Reserving ${tag(restaurantId)}: No table could be reserved for group $groupId.",
    )

    /**
     * Reports that the tables [oldIds] were merged into [mergedId] for group [groupId].
     */
    fun mergingTables(restaurantId: Int, groupId: Int, oldIds: Collection<Int>, mergedId: Int) = LogSink.write(
        LogLevel.INFO,
        "FOH Merging Tables ${tag(restaurantId)}: For group $groupId the tables ${LogFormat.ids(oldIds)} " +
            "were merged into $mergedId.",
    )

    /**
     * Reports that group [groupId] was seated at table [tableId] by the waitstaff [waiterIds].
     */
    fun seating(restaurantId: Int, groupId: Int, tableId: Int, waiterIds: Collection<Int>) = LogSink.write(
        LogLevel.IMPORTANT,
        "FOH Seating ${tag(restaurantId)}: Group $groupId seated at table $tableId " +
            "by waitstaff ${LogFormat.ids(waiterIds)}.",
    )

    /**
     * Reports that no free waitstaff was available for group [groupId].
     */
    fun noSeatingNoWaiter(restaurantId: Int, groupId: Int) = LogSink.write(
        LogLevel.INFO,
        "FOH No Seating ${tag(restaurantId)}: No free waitstaff available for group $groupId.",
    )

    /**
     * Reports that waitstaff [waiterId] was assigned to group [groupId] but no table was available.
     */
    fun noSeatingNoTable(restaurantId: Int, waiterId: Int, groupId: Int) = LogSink.write(
        LogLevel.INFO,
        "FOH No Seating ${tag(restaurantId)}: Assigned waitstaff $waiterId but no table available, " +
            "group $groupId is sent away.",
    )

    /**
     * Reports the order [orderId] of group [groupId]; [waiterId] is null for a delivery order.
     */
    fun ordering(restaurantId: Int, groupId: Int, orderId: Int, dishes: Map<String, Int>, waiterId: Int?) {
        val head = "FOH Ordering ${tag(restaurantId)}: Group $groupId placed order $orderId of ${
            LogFormat.mapping(dishes)
        }"
        val tail = if (waiterId == null) "." else " with waitstaff $waiterId."
        LogSink.write(LogLevel.IMPORTANT, head + tail)
    }

    /**
     * Reports that [customers] customers of group [groupId] could not place an order.
     */
    fun noOrdering(restaurantId: Int, groupId: Int, customers: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "FOH No Ordering ${tag(restaurantId)}: Group $groupId could not place an order for $customers customers, " +
            "they leave the restaurant.",
    )

    /**
     * Reports how many [waiters] seated how many [customers] on how many [tables] this tick.
     */
    fun seatingStatus(restaurantId: Int, waiters: Int, customers: Int, tables: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH Seating Status ${tag(restaurantId)}: $waiters waitstaff seated $customers customers on $tables tables.",
    )

    /**
     * Reports how many [customers] ordered and how many [waiters] took orders this tick.
     */
    fun orderingStatus(restaurantId: Int, customers: Int, waiters: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH Ordering Status ${tag(restaurantId)}: The restaurant received orders from $customers customers, " +
            "$waiters waitstaff took orders.",
    )

    /**
     * Reports that waitstaff [waiterId] served [dishes] to table [tableId].
     */
    fun serving(restaurantId: Int, waiterId: Int, dishes: Map<String, Int>, tableId: Int, ticksAfterOrder: Int) =
        LogSink.write(
            LogLevel.IMPORTANT,
            "FOH Serving ${tag(restaurantId)}: Waitstaff $waiterId serves ${LogFormat.mapping(dishes)} " +
                "to table $tableId $ticksAfterOrder ticks after ordering.",
        )

    /**
     * Reports that waitstaff [waiterId] did not serve [meals] meals to table [tableId].
     */
    fun noServing(restaurantId: Int, waiterId: Int, meals: Int, tableId: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH No Serving ${tag(restaurantId)}: Waitstaff $waiterId did not serve $meals meals to table $tableId.",
    )

    /**
     * Reports that waitstaff [waiterId] handed [dishes] of order [orderId] to driver [driverId].
     */
    fun deliveryHandover(restaurantId: Int, waiterId: Int, dishes: Map<String, Int>, driverId: Int, orderId: Int) =
        LogSink.write(
            LogLevel.IMPORTANT,
            "FOH Delivery ${tag(restaurantId)}: Waitstaff $waiterId serves ${LogFormat.mapping(dishes)} meals " +
                "to driver $driverId for order $orderId.",
        )

    /**
     * Reports how many [waiters] served how many [meals] in total this tick.
     */
    fun servingStatus(restaurantId: Int, waiters: Int, meals: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH Serving Status ${tag(restaurantId)}: $waiters waitstaff served $meals meals.",
    )

    /**
     * Reports that waitstaff [waiterId] escorted [customers] customers of group [groupId] outside.
     */
    fun escorting(restaurantId: Int, waiterId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "FOH Escorting ${tag(restaurantId)}: Waitstaff $waiterId escorts $customers customers of group $groupId " +
            "from table $tableId outside.",
    )

    /**
     * Reports how many [waiters] escorted how many [customers] this tick.
     */
    fun escortingStatus(restaurantId: Int, waiters: Int, customers: Int) = LogSink.write(
        LogLevel.DEBUG,
        "FOH Escorting Status ${tag(restaurantId)}: $waiters waitstaff escorted $customers customers this tick.",
    )
}
