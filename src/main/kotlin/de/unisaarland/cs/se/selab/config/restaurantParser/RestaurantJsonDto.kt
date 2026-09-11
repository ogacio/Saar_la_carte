package de.unisaarland.cs.se.selab.config.restaurantParser
import kotlinx.serialization.Serializable

@Serializable
data class RestaurantJsonDto (
    public val id: Int,
    public val name: String,
    public val type:String,
    public val openingTickStart: Int,
    public val openingTickEnd: Int,
    public val deliveryDrivers: Int,
    public val event:Boolean,
    public val positiveRatings: Int,
    public val negativeRatings: Int,
    public val recipes:MutableList<Int>,
    public val kitchenStaff:Map<String, Int>,
    public val waitstaff: Int,
    public val tables:MutableList<TableDto>,
)
