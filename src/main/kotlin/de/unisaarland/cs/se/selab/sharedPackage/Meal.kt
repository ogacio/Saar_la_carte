package de.unisaarland.cs.se.selab.sharedPackage
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer

data class Meal (
    val orderId : Int,
    val customer : Customer,
    val recipe : Recipe,
    val finishedTick : Int? = null,
    var status : MealStatus = MealStatus.QUEUED
)