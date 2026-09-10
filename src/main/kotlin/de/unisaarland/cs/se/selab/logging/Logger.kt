package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.incident.IncidentType
import java.io.PrintWriter

/**
 * The entry point of the logging package: configuration, initialization, the simulation lifecycle
 * and the final statistics.
 *
 * The remaining statements live in [CustomerLogger], [FohLogger], [KitchenLogger] and
 * [DeliveryLogger]; all of them write through the same [LogSink], so the split is invisible in the
 * produced log.
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
        "Restaurant Start ${LogFormat.restaurantTag(restaurantId)}: Restaurant $restaurantId simulates a tick.",
    )

    /**
     * Reports that restaurant [restaurantId] finished simulating the current tick.
     */
    fun restaurantEnd(restaurantId: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Restaurant End ${LogFormat.restaurantTag(restaurantId)}: " +
            "Restaurant $restaurantId finished simulating the tick.",
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
    fun statRatings(restaurantId: Int, ratings: Int) =
        LogSink.write(LogLevel.IMPORTANT, "Simulation Statistics: Restaurant $restaurantId received $ratings ratings.")
}
