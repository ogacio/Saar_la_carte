package de.unisaarland.cs.se.selab.foh.visit

import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerStatus

class AwaitingMealState : VisitState() {

    /** Hands the meals to their customers. Partial serving keeps the table here. */
    override fun onServed(visit: Visit, meals: List<Meal>, tick: Int) {
        for (meal in meals) {
            meal.status = MealStatus.SERVED
            meal.customer.receive(meal, tick)
        }
        if (visit.customersWaitingForFood().isEmpty()) visit.state = EatingUpState()
    }

    override fun onTickElapsed(visit: Visit, tick: Int) {
        visit.recordFinishedEaters(tick)
        val placed = visit.orderedTick ?: return
        val waited = tick - placed

        val servedSomebody = visit.group.members().any { it.servedTick() != null }
        if (!servedSomebody) {
            if (waited >= PATIENCE_TICKS) {
                val everyone = visit.customersInside()
                visit.leftUnservedThisTick = everyone.size
                visit.leaveUnserved(everyone)
                visit.failedAttempt = true
                visit.state = GoneState()
            }
            return
        }

        if (waited >= PATIENCE_TICKS + EXTRA_PATIENCE_TICKS) {
            val unserved = visit.customersWaitingForFood()
            visit.leftUnservedThisTick = unserved.size
            visit.leaveUnserved(unserved)
        }
        if (visit.customersWaitingForFood().isEmpty()) {
            val allFinished = visit.customersInside().all { it.status() == CustomerStatus.DONE_EATING }
            visit.state = if (allFinished) ReadyToLeaveState() else EatingUpState()
        }
    }

    private companion object {
        /** "Customers ... wait for up to 5 ticks for their food to be SERVED." */
        const val PATIENCE_TICKS = 5

        /** "If at least one person on the table has received their meal, they wait for 2 more ticks." */
        const val EXTRA_PATIENCE_TICKS = 2
    }
}
