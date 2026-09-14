package de.unisaarland.cs.se.selab.foh.visit


class AwaitingMealState : VisitState() {

    const val TOLERATE_TICKS = 5

    override fun onMealCooked(visit: Visit, meal: Meal, tick: Int) {
        if (visit.firstMealTick == null) v.firstMealTick = tick
    }

    /** Hands the meals to their customers. Partial serving keeps the table here. */
    override fun onServed(visit: Visit, meals: List<Meal>, tick: Int) {
        for (meal in meals) {
            meal.markServed()
            meal.customer.receive(meal, tick)
        }
        if (visit.customersWaitingForFood().isEmpty()) visit.state = EatingUpState()
    }

    override fun onTickElapsed(visit: Visit, tick: Int) {

        val placed = visit.orderedTick ?: return
        val waited = tick - placed

        val servedSomebody = visit.group.members.any { it.servedTick != null }
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
            val allFinished = visit.customersInside().all { it.status == CustomerStatus.DONE_EATING }
            visit.state = if (allFinished) ReadyToLeaveState() else EatingUpState()
        }

    }
}