package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.simulation.Supplier

class Kitchen (
    val roaster : CookRoaster,
    val pantry : Pantry,
    var queue : MutableList<Order>,
    val reservationBook : ReservationBook
    )

{
    // called by foh, puts the order into the queue
    fun enqueue(o:Order) {
        queue.add(o)
    }

    /**
     called by foh, at the end of the ordering service
    responsible for 1.updating the queue with the cooked meals 2. starting the cooking from the queue
    1. -> calling finished() on roaster
    2. -> making MutableList<Meal> from the queue with the same meals inside,
    then start calling roaster.startCooking on all
     */
    fun cook() {
        roaster.finished()
        var mealsToCookByRecipe : MutableList<Pair<Recipe, MutableList<Meal>>>
        for (o in queue) {
            for (m in o.meals) {
                if(m.status == MealStatus.QUEUED && roaster.hasEligible(m.recipe)) {
                    if(mealsToCookByRecipe.contains(Pair(m.recipe,_))) {}

                }
            }
        }

        // we have to reserve ingredients here
    }

    // called by Restaurant -> prepare, gets the supplies from Supplier into the Pantry
    fun planEvening(regulars : MutableList<CustomerGroup>, otherSeats : Int, supplier : Supplier) {
        pantry.checkDateAndCleanOut()

    }

    fun canCook(r:Recipe) : Boolean {
        return roaster.hasEligible(r)
    }

    // called by incident, changes the cooks
    fun changeStaff(type:CookType,delta: Int) {
        roaster.changeStaff(type, delta)
    }

    // called at the end of the evening by Restaurant -> closeEvening(), resets everything
    fun closeEvening() {
        roaster.resetEvening()
        queue.clear()
    }
}