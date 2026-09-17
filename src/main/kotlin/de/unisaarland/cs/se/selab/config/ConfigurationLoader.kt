package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.config.restaurantParser.RestaurantParser
import de.unisaarland.cs.se.selab.config.scenarioParser.ScenarioParser
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.simulation.BrowsingService
import de.unisaarland.cs.se.selab.simulation.CustomerRegistry
import de.unisaarland.cs.se.selab.simulation.Simulator
import de.unisaarland.cs.se.selab.simulation.ratings.RatingBook
import java.io.File

/**
 * Reads the three configuration files and builds the simulation from them (F01).
 */
class ConfigurationLoader(
    private val foodPath: String,
    private val restaurantsPath: String,
    private val scenarioPath: String,
    private val maxTicks: Int,
) {
    private val model = ParsedModel()

    /**
     * Parses the three files in the order the spec requires and returns the simulation,
     * or null as soon as one file is invalid.
     */
    fun load(): Simulator? {
        if (!runStage(foodPath) { FoodParser(model).parse(it) }) return null
        if (!runStage(restaurantsPath) { RestaurantParser(model).parse(it) }) return null
        if (!runStage(scenarioPath) { ScenarioParser(model).parse(it) }) return null
        return buildSimulator()
    }

    /**
     * Runs one parser on [path] and logs whether the file was valid. The parser is passed as a
     * function, so the three parsers only need a `parse(path): Boolean`.
     */
    private fun runStage(path: String, parse: (String) -> Boolean): Boolean {
        val valid = parse(path)
        val fileName = File(path).name
        if (valid) Logger.configParsed(fileName) else Logger.configInvalid(fileName)
        return valid
    }

    /**
     * Takes the restaurants the parser built (in ascending id), seeds each one's initial ratings
     * from the restaurants file into the simulation's [RatingBook], and creates the simulator.
     */
    private fun buildSimulator(): Simulator {
        val restaurants = model.allRestaurants()
        for (restaurant in restaurants) {
            RatingBook.initializeRatings(
                restaurant.getId(),
                restaurant.initialPositiveRatings,
                restaurant.initialNegativeRatings
            )
        }
        return Simulator(
            maxTicks,
            restaurants,
            CustomerRegistry(model.allCustomerGroups()),
            model.allIncidents(),
            BrowsingService(mutableListOf(), RatingBook),
        )
    }
}
