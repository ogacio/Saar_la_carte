package de.unisaarland.cs.se.selab.config.scenarioParser

import kotlinx.serialization.Serializable

/** One entry of the `incidents` array of the scenario file. */
@Serializable
data class IncidentJsonDto(
    val id: Int,
    val type: String,
    val evening: Int,
    val restaurant: Int? = null,
    val number: Int? = null,
    val staffType: String? = null,
    val cookType: String? = null,
    val ingredient: String? = null,
    val adaptation: Int? = null,
    val packagingVolume: Int? = null,
    val duration: Int? = null,
)
