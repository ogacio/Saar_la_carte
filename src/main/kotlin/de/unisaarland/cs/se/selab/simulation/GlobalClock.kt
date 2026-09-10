package de.unisaarland.cs.se.selab.simulation

//* Simple global clock object */
object GlobalClock {
    private var currentTick: Int = 0
    private var currentEvening: Int = 0

    public fun advanceTick() {
        currentTick++
    }

    public fun advanceEvening() {
        currentEvening++
    }

}