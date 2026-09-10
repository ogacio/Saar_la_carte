package de.unisaarland.cs.se.selab.logging

/**
 * The statements of the delivery service, in the order in which a driver passes through them.
 */
object DeliveryLogger {
    private fun tag(restaurantId: Int) = LogFormat.restaurantTag(restaurantId)

    /**
     * Reports that driver [driverId] prepares to drive order [orderId] to group [groupId].
     */
    fun deliveryPreparation(restaurantId: Int, driverId: Int, orderId: Int, groupId: Int, ticks: Int) = LogSink.write(
        LogLevel.INFO,
        "Delivery Preparation ${tag(restaurantId)}: Driver $driverId prepares driving order $orderId " +
            "to group $groupId, which will take $ticks ticks.",
    )

    /**
     * Reports that driver [driverId] drove [distance] km and needs [ticksLeft] more ticks.
     */
    fun deliveryDriving(restaurantId: Int, driverId: Int, distance: Int, ticksLeft: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Delivery Driving ${tag(restaurantId)}: Driver $driverId drove $distance km and needs $ticksLeft more ticks.",
    )

    /**
     * Reports that driver [driverId] arrived at group [groupId] with order [orderId].
     */
    fun deliveryArrival(restaurantId: Int, driverId: Int, groupId: Int, orderId: Int) = LogSink.write(
        LogLevel.INFO,
        "Delivery Arrival ${tag(restaurantId)}: Driver $driverId arrived at group $groupId with order $orderId.",
    )

    /**
     * Reports that driver [driverId] handed order [orderId] over to group [groupId].
     */
    fun deliveryFinished(restaurantId: Int, driverId: Int, orderId: Int, groupId: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Delivery Finished ${tag(restaurantId)}: Driver $driverId gave delivery of order $orderId to group $groupId.",
    )

    /**
     * Reports that driver [driverId] failed to deliver order [orderId] to group [groupId].
     */
    fun deliveryFailed(restaurantId: Int, driverId: Int, orderId: Int, groupId: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Delivery Failed ${tag(restaurantId)}: Driver $driverId failed to deliver order $orderId to group $groupId.",
    )

    /**
     * Reports that group [groupId] gave up on waiting for order [orderId].
     */
    fun deliveryGivenUp(restaurantId: Int, groupId: Int, orderId: Int) = LogSink.write(
        LogLevel.INFO,
        "Delivery Given Up ${tag(restaurantId)}: Group $groupId gave up on waiting for delivery of order $orderId.",
    )

    /**
     * Reports that driver [driverId] has returned to the restaurant.
     */
    fun deliveryReturned(restaurantId: Int, driverId: Int) = LogSink.write(
        LogLevel.INFO,
        "Delivery Returned ${tag(restaurantId)}: Driver $driverId has returned.",
    )

    /**
     * Reports that group [groupId] finished eating its delivery.
     */
    fun deliveryFinishedEating(restaurantId: Int, groupId: Int) = LogSink.write(
        LogLevel.INFO,
        "Delivery Finished Eating ${tag(restaurantId)}: Group $groupId has finished eating.",
    )
}
