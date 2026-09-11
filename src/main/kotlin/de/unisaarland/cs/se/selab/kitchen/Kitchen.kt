package de.unisaarland.cs.se.selab.kicthen
import de.unisaarland.cs.se.selab.shared_data.Meal
import de.unisaarland.cs.se.selab.shared_data.CustomerGroup
import de.unisaarland.cs.se.selab.shared_data.Recipe
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.shared_data.Order

class Kitchen (
    val roster:CookRoster,
    val pantry:Pantry,
    var queue:MutableList<Order>,
    val reservationBook:ReservationBook,
    val clock:GlobalClock
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

    }
}