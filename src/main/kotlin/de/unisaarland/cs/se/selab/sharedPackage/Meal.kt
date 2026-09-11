package de.unisaarland.cs.se.selab.sharedPackage

data class Meal (
    val order:Order,
    val customer:Customer,
    val recipe:Recipe,
    val finishedTick: Int?,
    val status:MealStatus
)