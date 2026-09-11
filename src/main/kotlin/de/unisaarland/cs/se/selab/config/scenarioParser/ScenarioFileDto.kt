package de.unisaarland.cs.se.selab.config.scenarioParser

import kotlinx.serialization.Serializable

/** The root object of the scenario configuration file. */
@Serializable
data class ScenarioFileDto(
    val customerGroups: MutableList<CustomerGroupJsonDto>,
    val incidents: MutableList<IncidentJsonDto>,
)
