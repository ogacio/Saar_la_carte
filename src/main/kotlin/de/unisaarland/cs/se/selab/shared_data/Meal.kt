package de.unisaarland.cs.se.selab.shared_data

data class Meal (
    val order:Order,
    val customer:Customer,
    val recipe:Recipe,
    val finishedTick: Int?,
    val status:MealStatus
)