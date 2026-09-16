package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

/**
 * a list of restaurant json files that we need to parse
 */
@Serializable
data class RestaurantFileDto(
    public val restaurants: MutableList<RestaurantJsonDto>
)
