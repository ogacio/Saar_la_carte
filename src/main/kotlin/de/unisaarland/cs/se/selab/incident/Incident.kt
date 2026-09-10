package de.unisaarland.cs.se.selab.incident

/**
 * The kind of an incident (specification, Figure 8).
 */
enum class IncidentType {
    STAFF,
    RECIPE,
    PACKAGING,
    UNAVAILABLE,
}

/**
 * Something that happens before an evening and changes the state of the simulation.
 *
 * Incidents are applied once, at the start of the evening they are scheduled for, in ascending order
 * of their id.
 */
abstract class Incident(
    val id: Int,
    val evening: Int,
) {
    /** The kind of this incident. */
    abstract val type: IncidentType

    /**
     * Applies the effect of this incident to [sim].
     */
    abstract fun apply(sim: SimulationContext)
}
