package de.unisaarland.cs.se.selab.testsupport

import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.logging.LogLevel
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RecipeIngredient
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CasualCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.Customer
import de.unisaarland.cs.se.selab.sharedPackage.customers.DeliveryPreference
import de.unisaarland.cs.se.selab.sharedPackage.customers.EventCustomerGroup
import de.unisaarland.cs.se.selab.sharedPackage.customers.FoodPreference
import de.unisaarland.cs.se.selab.sharedPackage.customers.RegularCustomerGroup
import de.unisaarland.cs.se.selab.simulation.SubUnits
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import org.mockito.kotlin.mock
import java.io.PrintWriter
import java.io.StringWriter

/** Shared builders for the unit tests; they only construct the real classes, never replace them. */
object Fixtures {
    const val RESTAURANT_ID = 1

    /** Points the logger at a fresh buffer and returns it. */
    fun captureLog(level: LogLevel = LogLevel.DEBUG): StringWriter {
        val buffer = StringWriter()
        Logger.configure(level, PrintWriter(buffer))
        return buffer
    }

    /** The non-blank lines written to [buffer] so far. */
    fun logLines(buffer: StringWriter): List<String> = buffer.toString().lines().filter { it.isNotBlank() }

    /** [size] customers, all following [preference]. */
    fun members(size: Int, preference: FoodPreference? = null): List<Customer> = List(size) { Customer(preference) }

    fun regular(
        id: Int,
        size: Int,
        tableType: TableType = TableType.COMMON,
        restaurantId: Int = RESTAURANT_ID,
        visitingStart: Int = 1,
        visitingPeriod: Int = 1,
    ) = RegularCustomerGroup(
        id,
        size,
        tableType,
        1,
        members(size),
        emptyList(),
        visitingStart,
        visitingPeriod,
        restaurantId,
    )

    fun casual(
        id: Int,
        size: Int,
        tableType: TableType = TableType.COMMON,
        deliveryDistance: Int = 0,
        likelihood: RatingLikelihood = RatingLikelihood.ALWAYS,
        types: Set<RestaurantType> = setOf(RestaurantType.EUROPEAN),
        evenings: List<Int> = listOf(1),
        visitingTick: Int = 1,
        preference: FoodPreference? = null,
    ) = CasualCustomerGroup(
        id,
        size,
        tableType,
        visitingTick,
        members(size, preference),
        listOfNotNull(preference),
        types,
        evenings,
        DeliveryPreference(deliveryDistance, likelihood),
    )

    fun event(
        id: Int,
        size: Int,
        eventEvening: Int = 4,
        tableType: TableType = TableType.COMMON,
        types: Set<RestaurantType> = setOf(RestaurantType.EUROPEAN),
        visitingTick: Int = 1,
        preference: FoodPreference? = null,
    ) = EventCustomerGroup(
        id,
        size,
        tableType,
        visitingTick,
        members(size, preference),
        listOfNotNull(preference),
        types,
        eventEvening,
        emptyMap(),
    )

    fun recipe(id: Int, ingredients: List<RecipeIngredient> = emptyList()) =
        Recipe(id, "dish$id", 10, setOf(CookType.EXEC), ingredients.toMutableList(), null)

    fun subUnits(
        menu: Menu = mock(),
        pantry: Pantry = mock(),
        kitchen: Kitchen = mock(),
    ) = SubUnits(RESTAURANT_ID, menu, pantry, kitchen)
}
