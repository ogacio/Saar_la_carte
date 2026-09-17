package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

/**
 * describes what fields the JSON file has
 */
@Serializable
data class RestaurantJsonDto(
    val id: Int,
    val name: String,
    val type: String,
    val openingTickStart: Int,
    val openingTickEnd: Int,
    val deliveryDrivers: Int,
    val event: Boolean,
    val positiveRatings: Int,
    val negativeRatings: Int,
    val recipes: MutableList<Int>,
    val kitchenStaff: Map<String, Int>,
    val waitstaff: Int,
    val tables: MutableList<TableDto>,
)
