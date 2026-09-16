package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.foh.ReservationBook
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus

class Kitchen (
    val roaster : CookRoaster,
    val pantry : Pantry,
    var queue : MutableList<Order>,
    val reservationBook : ReservationBook
    )

{
    fun enqueue(o:Order) {
        queue.add(o)
    }

    fun cook() : MutableList<Meal> {// responsible for making MutableList<Meal> from the queue with the same meals inside,
                                    // then start calling roaster.startCooking on all
        for (o in queue) {
            for (m in o.meals) {
                if(m.status == MealStatus.QUEUED && roaster.hasEligible(m.recipe)) {
                    val allOfTypeM =
                    val willCook = roaster.startCooking(allOfTypeM)
                }
            }
        }

        // we have to reserve ingredients here (only if it is not delivery)
    }

    fun planEvening(expectedCostumers : MutableList<CustomerGroup>, otherSeats : Int) {
        pantry.checkDateAndCleanOut()

    }

    fun finishedMeals () : MutableList<Meal> {  // has to update queue
        var finished = roaster.finished()
    }

    fun canCook(r:Recipe) : Boolean {
        return roaster.hasEligible(r)
    }

    fun changeStaff(type:CookType,delta: Int) {
        roaster.changeStaff(type, delta)
    }

    fun closeEvening() {
        roaster.resetEvening()
        pantry.discardEvening()
        queue.clear()
    }
}