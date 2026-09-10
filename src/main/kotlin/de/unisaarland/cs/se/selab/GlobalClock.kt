package de.unisaarland.cs.se.selab

object GlobalClock {
    private var currentTick: Int = 0
    private var evening: Int = 0
    private var tickInEvening: Int = 0
    fun advanceTick(): Unit{
        currentTick++
        tickInEvening++
    }
    fun advanceEvening(): Unit {
        evening++
        tickInEvening = 0
    }
    fun getCurrentTick(): Int = currentTick
    fun getEvening(): Int = evening
    fun getTickInEvening(): Int = tickInEvening
}