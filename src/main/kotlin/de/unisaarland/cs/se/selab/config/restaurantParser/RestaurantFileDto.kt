package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantFileDto (
    public val restaurants:MutableList<RestaurantJsonDto>
)