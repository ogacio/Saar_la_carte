package de.unisaarland.cs.se.selab.kicthen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.sharedPackage.Pantry

class Kitchen (
    val roster:CookRoster,
    val pantry:Pantry,
    var queue:MutableList<Order>,
    val reservationBook:ReservationBook,
) {
    public fun enqueue(o:Order):Unit {

    }

    public fun cook():MutableList<Meal> {

    }

    public fun planEvening(expectedCostumers:MutableList<CustomerGroup>,otherSeats: Int):Unit {

    }

    public fun canCook(r:Recipe):Boolean {

    }

    public fun changeStaff(type:CookType,delta: Int):Unit {

    }

    public fun closeEvening():Unit {
        roster.resetEvening()
        queue.clear()
    }
}