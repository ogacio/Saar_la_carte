package de.unisaarland.cs.se.selab.logging

import de.unisaarland.cs.se.selab.shared.CookType
import de.unisaarland.cs.se.selab.shared.UnitType

/**
 * The statements of the pantry and the kitchen: restocking, assigning dishes and cooking.
 */
object KitchenLogger {
    private fun tag(restaurantId: Int) = LogFormat.restaurantTag(restaurantId)

    /**
     * Reports that [amount] [unit] of [name] were removed from the pantry because they expired.
     */
    fun pantryRemoved(restaurantId: Int, amount: Int, unit: UnitType, name: String) = LogSink.write(
        LogLevel.DEBUG,
        "Pantry ${tag(restaurantId)}: Removed $amount $unit of $name from the pantry.",
    )

    /**
     * Reports that [amount] [unit] of [name] were procured from the supplier.
     */
    fun procured(restaurantId: Int, amount: Int, unit: UnitType, name: String) = LogSink.write(
        LogLevel.DEBUG,
        "Pantry ${tag(restaurantId)}: Procured $amount $unit of $name from the supplier.",
    )

    /**
     * Reports that restaurant [restaurantId] finished restocking its pantry.
     */
    fun restocked(restaurantId: Int) =
        LogSink.write(LogLevel.INFO, "Pantry ${tag(restaurantId)}: Restocked ingredients.")

    /**
     * Reports that cook [cookId] starts cooking [meals] meals of [dishName].
     */
    fun dishAssignment(
        restaurantId: Int,
        cookId: Int,
        cookType: CookType,
        meals: Int,
        dishName: String,
        sourceOrderId: Int,
        allOrderIds: Collection<Int>,
    ) = LogSink.write(
        LogLevel.IMPORTANT,
        "Kitchen Dish Assignment ${tag(restaurantId)}: Cook $cookId of type $cookType starts cooking $meals meals " +
            "of dish $dishName based on order $sourceOrderId for orders ${LogFormat.ids(allOrderIds)}.",
    )

    /**
     * Reports that cook [cookId] finished [meals] meals of [dishName].
     */
    fun mealCooked(restaurantId: Int, cookId: Int, meals: Int, dishName: String, ticksAfterOrder: Int) =
        LogSink.write(
            LogLevel.IMPORTANT,
            "Kitchen Meal Cooked ${tag(restaurantId)}: Cook $cookId finished cooking $meals meals " +
                "of dish $dishName $ticksAfterOrder ticks after ordering.",
        )

    /**
     * Reports how many [cooks] cooked how many meals in [total], [finished] and [servable] this tick.
     */
    fun kitchenStatus(restaurantId: Int, cooks: Int, total: Int, finished: Int, servable: Int) = LogSink.write(
        LogLevel.DEBUG,
        "Kitchen Status ${tag(restaurantId)}: $cooks cooks were active cooking $total and finishing $finished " +
            "meals. $servable meals can be served by the waitstaff.",
    )
}
