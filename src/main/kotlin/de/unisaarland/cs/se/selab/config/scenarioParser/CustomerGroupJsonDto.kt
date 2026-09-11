package de.unisaarland.cs.se.selab.config.scenarioParser

import kotlinx.serialization.Serializable

/** One entry of the `foodPreferences` array of a customer group. */
@Serializable
data class FoodPreferenceDto(
    val size: Int,
    val excludedIngredients: MutableList<String>? = null,
    val preferredIngredients: MutableList<String>? = null,
    val favoriteDishes: MutableList<String>? = null,
)

/** One entry of the `customerGroups` array of the scenario file. */
@Serializable
data class CustomerGroupJsonDto(
    val id: Int,
    val type: String,
    val size: Int,
    val visitingTick: Int,
    val foodPreferences: MutableList<FoodPreferenceDto>,
    val tableType: String? = null,
    val visitingStart: Int? = null,
    val visitingPeriod: Int? = null,
    val restaurant: Int? = null,
    val visitingEvenings: MutableList<Int>? = null,
    val deliveryDistance: Int? = null,
    val ratingLikelihood: String? = null,
    val restaurantTypes: MutableList<String>? = null,
    val eventEvening: Int? = null,
    val favoriteDishes: Map<String, String>? = null,
)
