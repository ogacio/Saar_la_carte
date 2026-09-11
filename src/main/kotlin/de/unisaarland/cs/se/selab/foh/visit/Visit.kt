

class Visit(
    private val group: CustomerGroup,
) {
    private var state: VisitState = AwaitingSeatState
    var table: Table? = null
    var waiters: List<Waiter> = emptyList()
    var wasSeated: Boolean = false
    var order: Order? = null

    val seatingAttempts: Int
    val orderedTick: Int?
    val firstMealTick: Int?
    val rated: Boolean

    val leftUnservedThisTick: Int
    val finishedEatingThisTick: Int


    public fun seating(table: Table, waiter: Waiter, tick: Int): VisitState {
        // TODO
        return state
    }

    public fun seatingFailed(reservations: ReservationBook, sbu: SubUnits): VisitState {
        // TODO
        return state
    }

    public fun ordering(order: Order, tick: Int): VisitState {
        // TODO
        return state
    }

    public fun orderingFailed(sbu: SubUnits): VisitState {
        // TODO
        return state
    }

    public fun mealCooked(meal: Meal, tick: Int): VisitState {
        // TODO
        return state
    }

    public fun pending(action: ActionType, tick: Int): VisitState {
        // TODO
        return state
    }

    public fun serving(tick: Int): VisitState {
        // TODO
        return state
    }

    public fun advance(tick: Int): VisitState {
        // TODO
        return state
    }

    public fun escorting(sbu: SubUnits): VisitState {
        // TODO
        return state
    }

    public fun isFinished(): Boolean {
        // TODO
        return false
    }

    public fun experience(tick: Int): Int {
        // TODO
        return 0
    }

    public fun isRated(): Boolean {
        // TODO
        return false
    }

    public fun markRated(): VisitState {
        // TODO
        return state
    }
    
}