package de.unisaarland.cs.se.selab.config

import kotlinx.serialization.Serializable

/**
 * One entry of the `ingredients` array of the food configuration file (specification, Figure 4).
 */
@Serializable
data class IngredientDto(
    val name: String,
    val unit: String,
    val packagingVolume: Int,
    val bestBefore: Int,
)

/**
 * One entry of the `ingredients` array of a recipe (specification, Figure 5).
 *
 * The schema only requires `name` and `amount`; `unit` is accepted as well and, when present, has to
 * agree with the unit of the referenced ingredient.
 */
@Serializable
data class RecipeIngredientDto(
    val name: String,
    val amount: Int,
    val unit: String? = null,
)

/**
 * One entry of the `recipes` array of the food configuration file (specification, Figure 5).
 */
@Serializable
data class RecipeDto(
    val id: Int,
    val dishName: String,
    val duration: Int,
    val cookType: MutableList<String>,
    val ingredients: MutableList<RecipeIngredientDto>,
    val basicDishFor: String? = null,
)

/**
 * The root object of the food configuration file.
 */
@Serializable
data class FoodFileDto(
    val ingredients: MutableList<IngredientDto>,
    val recipes: MutableList<RecipeDto>,
)
