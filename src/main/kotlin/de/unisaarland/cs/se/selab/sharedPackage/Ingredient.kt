package de.unisaarland.cs.se.selab.sharedPackage

/**
 * One kind of ingredient the supplier sells and the restaurants cook with.
 *
 * The packaging volume is mutable because a PACKAGING incident changes it globally; the object is
 * shared by every recipe that uses the ingredient, so the change reaches all of them at once.
 */
class Ingredient(
    val name: String,
    val unit: UnitType,
    packagingVolume: Int,
    var bestUntil: Int,
) {
    /** The base amount in which the ingredient can be obtained from the supplier. */
    var packagingVolume: Int = packagingVolume
        private set

    /**
     * The number of whole packages needed to cover [amount] of this ingredient.
     */
    fun packagesFor(amount: Int): Int {
        if (amount <= 0) {
            return 0
        }
        return (amount + packagingVolume - 1) / packagingVolume
    }

    /**
     * Applies a PACKAGING incident by setting the packaging volume to [newVolume].
     */
    fun changePackaging(newVolume: Int) {
        if (newVolume > 0) {
            packagingVolume = newVolume
        }
    }
}
