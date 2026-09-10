package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.data.CustomerGroup
import de.unisaarland.cs.se.selab.data.Ingredient
import de.unisaarland.cs.se.selab.data.Recipe
import de.unisaarland.cs.se.selab.data.RestaurantType
import de.unisaarland.cs.se.selab.incidents.Incident
import de.unisaarland.cs.se.selab.shared_data.CustomerGroup
import de.unisaarland.cs.se.selab.shared_data.Ingredient
import de.unisaarland.cs.se.selab.shared_data.Recipe
import de.unisaarland.cs.se.selab.shared_data.RestaurantType
import de.unisaarland.cs.se.selab.simulation.Restaurant

/**
 * Model for storing parsed configuration data.
 */

class ParsedModel {

    private val ingredientsByName = mutableMapOf<String, Ingredient>()
    private val recipesById = mutableMapOf<Int, Recipe>()
    private val restaurantsById = mutableMapOf<Int, Restaurant>()
    private val customerGroupsById = mutableMapOf<Int, CustomerGroup>()
    private val incidentsById = mutableMapOf<Int, Incident>()

    // ------------------------------------------------------------------ lookup

    fun ingredient(name: String): Ingredient? = ingredientsByName[name]

    fun recipe(id: Int): Recipe? = recipesById[id]

    fun restaurant(id: Int): Restaurant? = restaurantsById[id]

    fun customerGroup(id: Int): CustomerGroup? = customerGroupsById[id]

    fun incident(id: Int): Incident? = incidentsById[id]

    // ------------------------------------------------------------ registration
    // Each returns false for a duplicate name or id, which makes the file invalid.
    private fun <K, V> register(map: MutableMap<K, V>, key: K, value: V): Boolean {
        if (map.containsKey(key)) {
            return false
        }
        map[key] = value
        return true
    }

    fun registerIngredient(i: Ingredient): Boolean = register(ingredientsByName, i.name, i)

    fun registerRecipe(r: Recipe): Boolean = register(recipesById, r.id, r)

    fun registerRestaurant(r: Restaurant): Boolean = register(restaurantsById, r.id, r)

    fun registerCustomerGroup(g: CustomerGroup): Boolean = register(customerGroupsById, g.id, g)

    fun registerIncident(i: Incident): Boolean = register(incidentsById, i.id, i)

    // ------------------------------------------------------------ cross-checks
    fun basicDishesFor(type: RestaurantType): Set<String> =
        recipesById.values.filter { it.basicDishFor == type }.map { it.dishName }.toSet()

    fun allIngredientNames(): Set<String> = ingredientsByName.keys.toSet()

    fun allDishNames(): Set<String> = recipesById.values.map { it.dishName }.toSet()

    // ------------------------------------------------------------ hand-off
    fun allRestaurants(): MutableList<Restaurant> =
        restaurantsById.values.sortedBy { it.id }.toMutableList()

    fun allCustomerGroups(): MutableList<CustomerGroup> =
        customerGroupsById.values.sortedBy { it.id }.toMutableList()

    fun allIncidents(): MutableList<Incident> =
        incidentsById.values.sortedBy { it.id }.toMutableList()
}
