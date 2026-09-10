package de.unisaarland.cs.se.selab

import de.unisaarland.cs.se.selab.config.ConfigurationLoader
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.logging.Logger
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File
import java.io.PrintWriter


private const val MAX_TICKS_LIMIT = 1000

private fun run(
    food: String, 
    restaurants: String, 
    scenario: String, 
    maxTicks: Int, 
    logLevel: LogLevel, 
    out: String?) {

        val writer = if (out != null) {
            PrintWriter(File(out))
        } else {
            PrintWriter(System.out)
        }

        writer.use {
            Logger.configure(logLevel, writer)
            val simulator = ConfigurationLoader(food, restaurants, scenario, maxTicks).load()
            simulator?.run()
        }
}


/**
 Main Function
 **/
fun main(args: Array<String>) { 
    val parser = ArgParser("simulation")

    val food by parser.option(
        ArgType.String,
        fullName = "food",
        description = "Path to the food file",
    ).required()
    val restaurants by parser.option(
        ArgType.String,
        fullName = "restaurants",
        description = "Path to the restaurants file",
    ).required()
    val scenario by parser.option(
        ArgType.String,
        fullName = "scenario",
        description = "Path to the scenario file",
    ).required()
    val maxTicks by parser.option(
        ArgType.Int,
        fullName = "maxTicks",
        description = "Maximum number of simulation ticks (0 to $MAX_TICKS_LIMIT)",
    ).required()
    val logLevel by parser.option(
        ArgType.String,
        fullName = "logLevel",
        description = "DEBUG, INFO or IMPORTANT",
    ).required()
    val out by parser.option(
        ArgType.String,
        fullName = "out",
        description = "Path to the output file (stdout if omitted)",
    )

    parser.parse(args)

    require(maxTicks in 0..MAX_TICKS_LIMIT) {
        "--maxTicks must be between 0 and $MAX_TICKS_LIMIT, was $maxTicks"
    }

    // Match against the enum names (case-sensitive), as the spec lists them.
    val level = requireNotNull(LogLevel.entries.firstOrNull { it.name == logLevel }) {
        "--logLevel must be DEBUG, INFO or IMPORTANT, was $logLevel"
    }

    run(food, restaurants, scenario, maxTicks, level, out)
}
