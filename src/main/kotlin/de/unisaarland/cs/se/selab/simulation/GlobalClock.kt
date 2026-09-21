package de.unisaarland.cs.se.selab.simulation

/**
 * The simulation time (F01). Only the Simulator moves it, with [advanceEvening] and
 * [advanceTick]; every other class only reads it.
 *
 * All counters start at 0, meaning "not started yet": the Simulator calls [advanceEvening]
 * before an evening and [advanceTick] at the start of each tick, so the first evening is 1 and
 * the ticks of an evening run from 1 to 24.
 */
object GlobalClock {
    /** The continuous tick over the whole simulation. Readable everywhere, changed only by [advanceTick]. */
    var currentTick: Int = 0
        private set

    /** The current evening. */
    private var evening: Int = 0

    /** The tick within the current evening, 1 to 24. */
    private var tickInEvening: Int = 0

    /** Starts the next tick: both tick counters move on by one. */
    fun advanceTick() {
        currentTick++
        tickInEvening++
    }

    /** Starts the next evening; its first tick begins with the next [advanceTick]. */
    fun advanceEvening() {
        evening++
        tickInEvening = 0
    }

    /** The current evening. */
    fun getEvening(): Int = evening

    /** The tick within the current evening. */
    fun getTickInEvening(): Int = tickInEvening
}
