package de.unisaarland.cs.se.selab.kicthen
import de.unisaarland.cs.se.selab.shared_data.Meal
import de.unisaarland.cs.se.selab.simulation.GlobalClock

class Cook (
    private val type : CookType,
    private var id: Int? = null,
    private var busyUntil: Int? = null,
    private var batch:MutableList<Meal>,
    private var clock:GlobalClock)
{
    fun startCooking(meals:MutableList<Meal>,tick: Int):Unit {

    }

    fun endCooking(tick: Int):MutableList<Meal> {

    }

    fun isFree(tick: Int):Boolean {

    }
}