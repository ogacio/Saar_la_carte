package de.unisaarland.cs.se.selab.shared_data

/**
 * The unit an [Ingredient] is measured in (specification, Figure 4).
 */
enum class UnitType {
    G,
    ML,
    X,
    ;

    /**
     * The spelling used in the configuration files and in the log messages.
     */
    override fun toString(): String = when (this) {
        G -> "g"
        ML -> "mL"
        X -> "X"
    }

    /** Factory for the JSON spellings of the units. */
    companion object {
        /**
         * Resolves the JSON spelling [raw] of a unit, or returns null if it is unknown.
         */
        fun from(raw: String): UnitType? = when (raw) {
            "g" -> G
            "mL" -> ML
            "X" -> X
            else -> null
        }
    }
}

