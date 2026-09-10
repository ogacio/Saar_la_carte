package de.unisaarland.cs.se.selab.shared

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
 * The kind of a restaurant, which determines its basic dishes (specification, Section 2.2).
 */
enum class RestaurantType {
    EUROPEAN,
    ASIAN,
    AFRICAN,
    AMERICAN,
}

/**
 * The kind of cook that is able to follow a [Recipe] (specification, Figure 5).
 */
enum class CookType {
    EXEC,
    SOUS,
    TOURNANT,
    SAUCE,
    FISH,
    ROAST,
    VEGETABLE,
    PASTRY,
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

/**
 * The kind of staff a staff change incident affects (specification, Figure 8).
 */
enum class StaffType {
    COOK,
    WAITSTAFF,
    DRIVER,
}

/**
 * How a customer group perceived its visit (specification, Section 2.2, "Rating").
 */
enum class Experience {
    NEGATIVE,
    NEUTRAL,
    POSITIVE,
}

/**
 * The rating a customer group leaves with the rating service.
 */
enum class Rating {
    POSITIVE,
    NEGATIVE,
}

/**
 * How likely a CASUAL customer group is to rate its experience (specification, Figure 6).
 */
enum class RatingLikelihood {
    NEVER,
    SOME,
    ALWAYS,
}
