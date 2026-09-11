package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.incident.IncidentType
import de.unisaarland.cs.se.selab.sharedPackage.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Rating
import de.unisaarland.cs.se.selab.sharedPackage.UnitType
import java.io.PrintWriter

/**
 * The restaurant tag of restaurant scoped statements, shared by every group below.
 */
private fun tag(restaurantId: Int) = LogFormat.restaurantTag(restaurantId)

/**
 * The single entry point of the logging package.
 *
 * It carries the statements that are not scoped to one restaurant acting in a tick: the
 * configuration, the initialization, the simulation lifecycle and the final statistics. Everything
 * else is grouped by area into the nested [Customer], [Foh], [Kitchen] and [Delivery] objects, so
 * that no object exceeds the detekt limit of 33 functions while callers still reach every statement
 * through `Logger`.
 *
 * All groups write through the same [LogSink], so the grouping is invisible in the produced log.
 */
object Logger {
    /**
     * Sends every following statement of at least [level] to [out].
     */
    fun configure(level: LogLevel, out: PrintWriter) = LogSink.configure(level, out)

    /**
     * Reports that the configuration file [fileName] was parsed and validated successfully.
     */
    fun configParsed(fileName: String) =
        LogSink.write(LogLevel.INFO, "Initialization Info: $fileName successfully parsed and validated.")

    /**
     * Reports that the configuration file [fileName] is invalid.
     */
    fun configInvalid(fileName: String) =
        LogSink.write(LogLevel.IMPORTANT, "Initialization Info: $fileName is invalid.")

    /**
     * Reports the start of the simulation.
     */
    fun simulationStarted() = LogSink.write(LogLevel.INFO, "Simulation Info: Simulation started.")

    /**
     * Reports that incident [incidentId] of [type] occurred before [evening].
     */
    fun incidentOccurred(incidentId: Int, type: IncidentType, evening: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Incident: Incident $incidentId of type $type occurred before evening $evening.",
    )

    /**
     * Reports the start of the preparation phase of [evening].
     */
    fun preparationStarted(evening: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Preparation: Preparation for evening $evening starts.")

    /**
     * Reports the start of the serving phase of [evening].
     */
    fun servingStarted(evening: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Serving: Serving of evening $evening starts.")

    /**
     * Reports the start of the continuous tick [tick], which is tick [tickInEvening] of its evening.
     */
    fun tickStarted(tick: Int, tickInEvening: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Simulation: Tick $tick ($tickInEvening) started.")

    /**
     * Reports the end of the serving phase of [evening].
     */
    fun servingEnded(evening: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Serving: Serving of evening $evening ends.")

    /**
     * Reports that restaurant [restaurantId] starts simulating the current tick.
     */
    fun restaurantStart(restaurantId: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Restaurant Start ${tag(restaurantId)}: Restaurant $restaurantId simulates a tick.",
    )

    /**
     * Reports that restaurant [restaurantId] finished simulating the current tick.
     */
    fun restaurantEnd(restaurantId: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Restaurant End ${tag(restaurantId)}: Restaurant $restaurantId finished simulating the tick.",
    )

    /**
     * Reports that the simulation statistics are being calculated.
     */
    fun statisticsCalculated() =
        LogSink.write(LogLevel.IMPORTANT, "Simulation Info: Simulation statistics are calculated.")

    /**
     * Reports that restaurant [restaurantId] cooked [meals] meals in total.
     */
    fun statMealsCooked(restaurantId: Int, meals: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Simulation Statistics: Restaurant $restaurantId cooked $meals meals.")

    /**
     * Reports that restaurant [restaurantId] served [customers] customers in total.
     */
    fun statCustomersServed(restaurantId: Int, customers: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Simulation Statistics: Restaurant $restaurantId served $customers customers.",
    )

    /**
     * Reports that restaurant [restaurantId] delivered meals to [customers] customers in total.
     */
    fun statCustomersDelivered(restaurantId: Int, customers: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Simulation Statistics: Restaurant $restaurantId delivered meals to $customers customers.",
    )

    /**
     * Reports that restaurant [restaurantId] received [ratings] ratings in total.
     */
    fun statRatings(restaurantId: Int, ratings: Int) = LogSink.write(
        LogLevel.IMPORTANT,
        "Simulation Statistics: Restaurant $restaurantId received $ratings ratings.",
    )

    /**
     * The statements about what the customer groups do: deciding on a restaurant, arriving, eating
     * and rating.
     */
    object Customer {
        /**
         * Reports that group [groupId] decided on restaurant [restaurantId].
         */
        fun restaurantDecision(groupId: Int, restaurantId: Int) = LogSink.write(
            LogLevel.DEBUG,
            "Restaurant Decision: Group $groupId decided on restaurant $restaurantId.",
        )

        /**
         * Reports that group [groupId] could not decide for a restaurant.
         */
        fun noRestaurantDecision(groupId: Int) = LogSink.write(
            LogLevel.DEBUG,
            "Restaurant No Decision: Group $groupId could not decide for a restaurant.",
        )

        /**
         * Reports that group [groupId] arrived at restaurant [restaurantId].
         */
        fun arrival(restaurantId: Int, groupId: Int) = LogSink.write(
            LogLevel.INFO,
            "Restaurant Arrival ${tag(restaurantId)}: Group $groupId arrived at restaurant $restaurantId.",
        )

        /**
         * Reports that [customers] customers of group [groupId] leave table [tableId] unserved.
         */
        fun noEating(restaurantId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
            LogLevel.INFO,
            "Restaurant No Eating ${tag(restaurantId)}: $customers customers of group $groupId " +
                "leave table $tableId due to not being served.",
        )

        /**
         * Reports that [customers] customers of group [groupId] finished eating at table [tableId].
         */
        fun finishedEating(restaurantId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
            LogLevel.INFO,
            "FOH Finished Eating ${tag(restaurantId)}: $customers customers of group $groupId " +
                "have finished eating at table $tableId.",
        )

        /**
         * Reports how many customers are still [eating] and how many have [eaten] this tick.
         */
        fun eatingStatus(restaurantId: Int, eating: Int, eaten: Int) = LogSink.write(
            LogLevel.DEBUG,
            "FOH Eating Status ${tag(restaurantId)}: $eating customers are eating and " +
                "$eaten customers have finished eating this tick.",
        )

        /**
         * Reports the [rating] group [groupId] left and the resulting counts of the restaurant.
         */
        fun rating(restaurantId: Int, groupId: Int, rating: Rating, positiveRatings: Int, negativeRatings: Int) =
            LogSink.write(
                LogLevel.INFO,
                "Rating ${tag(restaurantId)}: Group $groupId rates the restaurant $restaurantId " +
                    "with $rating rating, leading to $positiveRatings positive ratings and " +
                    "$negativeRatings negative ratings.",
            )

        /**
         * Reports how many [groups] performed a rating this tick.
         */
        fun ratingStatus(restaurantId: Int, groups: Int) = LogSink.write(
            LogLevel.DEBUG,
            "Rating Status ${tag(restaurantId)}: $groups groups performed ratings this tick.",
        )
    }

    /**
     * The statements of the front of the house: reserving, seating, ordering, serving and escorting.
     */
    object Foh {
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
         * Reports that waitstaff [waiterId] was assigned to [groupId] but no table was available.
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
            "FOH No Ordering ${tag(restaurantId)}: Group $groupId could not place an order " +
                "for $customers customers, they leave the restaurant.",
        )

        /**
         * Reports how many [waiters] seated how many [customers] on how many [tables] this tick.
         */
        fun seatingStatus(restaurantId: Int, waiters: Int, customers: Int, tables: Int) = LogSink.write(
            LogLevel.DEBUG,
            "FOH Seating Status ${tag(restaurantId)}: $waiters waitstaff seated $customers customers " +
                "on $tables tables.",
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
            "FOH No Serving ${tag(restaurantId)}: Waitstaff $waiterId did not serve $meals meals " +
                "to table $tableId.",
        )

        /**
         * Reports that waitstaff [waiterId] handed [dishes] of [orderId] to driver [driverId].
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
         * Reports that waitstaff [waiterId] escorted [customers] customers of [groupId] outside.
         */
        fun escorting(restaurantId: Int, waiterId: Int, customers: Int, groupId: Int, tableId: Int) = LogSink.write(
            LogLevel.IMPORTANT,
            "FOH Escorting ${tag(restaurantId)}: Waitstaff $waiterId escorts $customers customers " +
                "of group $groupId from table $tableId outside.",
        )

        /**
         * Reports how many [waiters] escorted how many [customers] this tick.
         */
        fun escortingStatus(restaurantId: Int, waiters: Int, customers: Int) = LogSink.write(
            LogLevel.DEBUG,
            "FOH Escorting Status ${tag(restaurantId)}: $waiters waitstaff escorted $customers customers this tick.",
        )
    }

    /**
     * The statements of the pantry and the kitchen: restocking, assigning dishes and cooking.
     */
    object Kitchen {
        /**
         * Reports that [amount] [unit] of [name] were removed from the pantry because they expired.
         */
        fun pantryRemoved(restaurantId: Int, amount: Int, unit: UnitType, name: String) = LogSink.write(
            LogLevel.DEBUG,
            "Pantry ${tag(restaurantId)}: Removed $amount $unit of $name from the pantry.",
        )

        /**
         * Reports that [amount] [unit] of [name] were procured from the supplier.
         */
        fun procured(restaurantId: Int, amount: Int, unit: UnitType, name: String) = LogSink.write(
            LogLevel.DEBUG,
            "Pantry ${tag(restaurantId)}: Procured $amount $unit of $name from the supplier.",
        )

        /**
         * Reports that restaurant [restaurantId] finished restocking its pantry.
         */
        fun restocked(restaurantId: Int) =
            LogSink.write(LogLevel.INFO, "Pantry ${tag(restaurantId)}: Restocked ingredients.")

        /**
         * Reports that cook [cookId] starts cooking [meals] meals of [dishName].
         */
        fun dishAssignment(
            restaurantId: Int,
            cookId: Int,
            cookType: CookType,
            meals: Int,
            dishName: String,
            sourceOrderId: Int,
            allOrderIds: Collection<Int>,
        ) = LogSink.write(
            LogLevel.IMPORTANT,
            "Kitchen Dish Assignment ${tag(restaurantId)}: Cook $cookId of type $cookType starts cooking " +
                "$meals meals of dish $dishName based on order $sourceOrderId " +
                "for orders ${LogFormat.ids(allOrderIds)}.",
        )

        /**
         * Reports that cook [cookId] finished [meals] meals of [dishName].
         */
        fun mealCooked(restaurantId: Int, cookId: Int, meals: Int, dishName: String, ticksAfterOrder: Int) =
            LogSink.write(
                LogLevel.IMPORTANT,
                "Kitchen Meal Cooked ${tag(restaurantId)}: Cook $cookId finished cooking $meals meals " +
                    "of dish $dishName $ticksAfterOrder ticks after ordering.",
            )

        /**
         * Reports how many [cooks] cooked how many meals in [total] and [finished] this tick.
         */
        fun kitchenStatus(restaurantId: Int, cooks: Int, total: Int, finished: Int, servable: Int) = LogSink.write(
            LogLevel.DEBUG,
            "Kitchen Status ${tag(restaurantId)}: $cooks cooks were active cooking $total and finishing " +
                "$finished meals. $servable meals can be served by the waitstaff.",
        )
    }

    /**
     * The statements of the delivery service, in the order in which a driver passes through them.
     */
    object Delivery {
        /**
         * Reports that driver [driverId] prepares to drive order [orderId] to group [groupId].
         */
        fun deliveryPreparation(
            restaurantId: Int,
            driverId: Int,
            orderId: Int,
            groupId: Int,
            ticks: Int,
        ) = LogSink.write(
            LogLevel.INFO,
            "Delivery Preparation ${tag(restaurantId)}: Driver $driverId prepares driving order $orderId " +
                "to group $groupId, which will take $ticks ticks.",
        )

        /**
         * Reports that driver [driverId] drove [distance] km and needs [ticksLeft] more ticks.
         */
        fun deliveryDriving(restaurantId: Int, driverId: Int, distance: Int, ticksLeft: Int) = LogSink.write(
            LogLevel.DEBUG,
            "Delivery Driving ${tag(restaurantId)}: Driver $driverId drove $distance km " +
                "and needs $ticksLeft more ticks.",
        )

        /**
         * Reports that driver [driverId] arrived at group [groupId] with order [orderId].
         */
        fun deliveryArrival(restaurantId: Int, driverId: Int, groupId: Int, orderId: Int) = LogSink.write(
            LogLevel.INFO,
            "Delivery Arrival ${tag(restaurantId)}: Driver $driverId arrived at group $groupId " +
                "with order $orderId.",
        )

        /**
         * Reports that driver [driverId] handed order [orderId] over to group [groupId].
         */
        fun deliveryFinished(restaurantId: Int, driverId: Int, orderId: Int, groupId: Int) = LogSink.write(
            LogLevel.IMPORTANT,
            "Delivery Finished ${tag(restaurantId)}: Driver $driverId gave delivery of order $orderId " +
                "to group $groupId.",
        )

        /**
         * Reports that driver [driverId] failed to deliver order [orderId] to group [groupId].
         */
        fun deliveryFailed(restaurantId: Int, driverId: Int, orderId: Int, groupId: Int) = LogSink.write(
            LogLevel.IMPORTANT,
            "Delivery Failed ${tag(restaurantId)}: Driver $driverId failed to deliver order $orderId " +
                "to group $groupId.",
        )

        /**
         * Reports that group [groupId] gave up on waiting for order [orderId].
         */
        fun deliveryGivenUp(restaurantId: Int, groupId: Int, orderId: Int) = LogSink.write(
            LogLevel.INFO,
            "Delivery Given Up ${tag(restaurantId)}: Group $groupId gave up on waiting " +
                "for delivery of order $orderId.",
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
}
