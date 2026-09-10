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

/**
 * Where a table stands inside the front of the house (specification, Figure 3).
 */
enum class TableType {
    COMMON,
    BAR,
    SEPARATED,
}

/**
 * Whether a customer group is REGULAR, CASUAL or an EVENT (specification, Figure 6).
 */
enum class GroupType {
    REGULAR,
    EVENT,
    CASUAL,
}
