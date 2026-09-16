package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

/**
 * Table
 */

@Serializable
data class TableDto(
    public val id: Int,
    public val type: String,
    public val size: Int
)
