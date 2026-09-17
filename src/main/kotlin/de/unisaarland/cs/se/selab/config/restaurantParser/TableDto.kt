package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

/**
 * Table
 */

@Serializable
data class TableDto(
    val id: Int,
    val type: String,
    val size: Int
)
