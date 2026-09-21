package de.unisaarland.cs.se.selab.config

import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.Restaurant

/**
 * Everything read from the three configuration files, looked up by name or id (F01).
 * The parsers register what they read; ConfigurationLoader takes it over once all files are valid.
 */
class ParsedModel {

    private val ingredientsByName = mutableMapOf<String, Ingredient>()
    private val recipesById = mutableMapOf<Int, Recipe>()
    private val restaurantsById = mutableMapOf<Int, Restaurant>()
    private val customerGroupsById = mutableMapOf<Int, CustomerGroup>()
    private val incidentsById = mutableMapOf<Int, Incident>()
    private val restaurantNames = mutableSetOf<String>()

    // ------------------------------------------------------------------ lookup

    /** The ingredient called [name], or null if the food file has none. */
    fun ingredient(name: String): Ingredient? = ingredientsByName[name]

    /** The recipe with [id], or null if the food file has none. */
    fun recipe(id: Int): Recipe? = recipesById[id]

    /** The restaurant with [id], or null if the restaurants file has none. */
    fun restaurant(id: Int): Restaurant? = restaurantsById[id]

    /** The customer group with [id], or null if the scenario file has none. */
    fun customerGroup(id: Int): CustomerGroup? = customerGroupsById[id]

    /** The incident with [id], or null if the scenario file has none. */
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

    /** Stores [i] under its name; false if an ingredient with that name already exists. */
    fun registerIngredient(i: Ingredient): Boolean = register(ingredientsByName, i.name, i)

    /** Stores [r] under its id; false if a recipe with that id already exists. */
    fun registerRecipe(r: Recipe): Boolean = register(recipesById, r.getId(), r)

    /** Stores [r] under its id; false if a restaurant with that id already exists. */
    fun registerRestaurant(r: Restaurant): Boolean {
        if (restaurantNames.contains(r.name)) {
            return false
        }
        if (!register(restaurantsById, r.getId(), r)) {
            return false
        }
        restaurantNames.add(r.name)
        return true
    }

    /** Stores [g] under its id; false if a customer group with that id already exists. */
    fun registerCustomerGroup(g: CustomerGroup): Boolean = register(customerGroupsById, g.id(), g)

    /** Stores [i] under its id; false if an incident with that id already exists. */
    fun registerIncident(i: Incident): Boolean = register(incidentsById, i.id, i)

    // ------------------------------------------------------------ cross-checks
    /** The dish names of all recipes that are a basic dish for restaurants of [type]. */
    fun basicDishesFor(type: RestaurantType): Set<String> =
        recipesById.values.filter { it.basicDishFor == type }.map { it.getDishName() }.toSet()

    /** The names of all ingredients read so far. */
    fun allIngredientNames(): Set<String> = ingredientsByName.keys.toSet()

    /** The dish names of all recipes read so far. */
    fun allDishNames(): Set<String> = recipesById.values.map { it.getDishName() }.toSet()

    // ------------------------------------------------------------ hand-off
    /** All restaurants in ascending id, for the simulation. */
    fun allRestaurants(): MutableList<Restaurant> =
        restaurantsById.values.sortedBy { it.getId() }.toMutableList()

    /** All customer groups in ascending id, for the simulation. */
    fun allCustomerGroups(): MutableList<CustomerGroup> =
        customerGroupsById.values.sortedBy { it.id() }.toMutableList()

    /** All incidents in ascending id, for the simulation. */
    fun allIncidents(): MutableList<Incident> =
        incidentsById.values.sortedBy { it.id }.toMutableList()

    /** All recipes in ascending id. */
    fun allRecipes(): List<Recipe> = recipesById.values.sortedBy { it.getId() }
}
