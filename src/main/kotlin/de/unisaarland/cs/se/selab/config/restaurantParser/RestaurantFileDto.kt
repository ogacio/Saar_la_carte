package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

/**
 * a list of restaurant JSON files that we need to parse
 */
@Serializable
data class RestaurantFileDto(
    val restaurants: MutableList<RestaurantJsonDto>
)
