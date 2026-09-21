package de.unisaarland.cs.se.selab.config.restaurantParser

import de.unisaarland.cs.se.selab.config.ConfigParser
import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.foh.DeliveryDesk
import de.unisaarland.cs.se.selab.foh.DeliveryDriver
import de.unisaarland.cs.se.selab.foh.FohServices
import de.unisaarland.cs.se.selab.foh.FrontOfTheHouse
import de.unisaarland.cs.se.selab.foh.Table
import de.unisaarland.cs.se.selab.foh.TableAssignmentService
import de.unisaarland.cs.se.selab.foh.Waiter
import de.unisaarland.cs.se.selab.foh.WaiterAssignmentService
import de.unisaarland.cs.se.selab.foh.services.DiningService
import de.unisaarland.cs.se.selab.foh.services.EscortingService
import de.unisaarland.cs.se.selab.foh.services.OrderingService
import de.unisaarland.cs.se.selab.foh.services.RatingService
import de.unisaarland.cs.se.selab.foh.services.SeatingService
import de.unisaarland.cs.se.selab.foh.services.ServingService
import de.unisaarland.cs.se.selab.kitchen.CookRoaster
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.kitchen.Kitchen
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
import de.unisaarland.cs.se.selab.simulation.DeliveryService
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Restaurant
import de.unisaarland.cs.se.selab.simulation.SubUnits
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.collections.isNotEmpty
import kotlin.collections.map
import kotlin.collections.set

/**
 * parses / creates the restaurant objects
 */
class RestaurantParser(model: ParsedModel) : ConfigParser(model) {

    override val schemaPath: String = "/schema/restaurant.schema"

    override fun readEntities(path: String): Boolean {
        var returnValue = true
        val text = File(path).readText()
        val fileDto: RestaurantFileDto = try {
            Json.decodeFromString(text)
        } catch (_: SerializationException) {
            return false
        }
        if (!checkUniqueRestaurantIds(fileDto.restaurants)) { // may be double check, parse model also does something
            returnValue = false
        } else {
            for (restaurantDto in fileDto.restaurants) {
                val restaurant = serialiseRestaurant(restaurantDto)
                if (restaurant == null || !model.registerRestaurant(restaurant)) {
                    returnValue = false
                    break
                }
            }
        }

        return returnValue
    }

    override fun validateFileScope(): Boolean {
        var returnValue = true
        if (!checkAtLeastOneRestaurant()) returnValue = false
        val presentTypes = model.allRestaurants().map { it.type }.toSet()
        for (type in presentTypes) {
            if (!checkBasicDishCoverage(model.basicDishesFor(type))) {
                returnValue = false
                break
            }
        }
        return returnValue
    }

    private fun checkUniqueRestaurantIds(restaurants: List<RestaurantJsonDto>): Boolean {
        for (restaurant in restaurants) {
            var count = 0
            val resId: Int = restaurant.id
            for (r in restaurants) {
                if (resId == r.id) count++
            }
            if (count > 1) return false
        }
        return true
    }

    /**
     * helper function for readEntities, creates Restaurant objects
     */
    private fun serialiseRestaurant(rjd: RestaurantJsonDto): Restaurant? {
        val type = try {
            RestaurantType.valueOf(rjd.type)
        } catch (_: IllegalArgumentException) {
            null
        }

        if (type != null && checkAtLeastOneOfEach(rjd)) {
            val foh: FrontOfTheHouse?
            val pantry = Pantry(restaurantId = rjd.id)
            val kitchen: Kitchen?
            val menu: Menu?
            val data: RestaurantData?

            // create kitchen, foh and menu
            val kitchenStaff = serialiseCookCounts(rjd)
            val tables = serialiseTables(rjd)
            val recipes = resolveRecipes(rjd, type) // Passing 'type' to resolve basic dishes

            if (kitchenStaff != null && tables != null && recipes != null) {
                // create tableAssignmentService and reservationBook
                val tableAssignmentService = TableAssignmentService(tables)
                val reservationBook = ReservationBook(tableAssignmentService)

                // create kitchen
                val roaster = CookRoaster(kitchenStaff, restaurantId = rjd.id)
                kitchen = Kitchen(roaster, pantry, mutableListOf(), reservationBook, type)

                // create menu
                menu = Menu(recipes, pantry, kitchen)

                // create foh
                foh = createFoh(rjd, tableAssignmentService, type, menu, pantry, kitchen, reservationBook)

                // create restaurant data
                data = createData(rjd, tables, recipes, type)
            } else {
                kitchen = null
                foh = null
                menu = null
                data = null
            }

            if (foh != null && kitchen != null) {
                if (menu != null && data != null) {
                    kitchen.roaster.initialiseCooks()
                    return Restaurant(
                        rjd.name, rjd.event, rjd.positiveRatings,
                        rjd.negativeRatings, foh, kitchen, pantry, menu, data
                    )
                }
            }
        }
        return null
    }

    /**
     * helper functions for serialiseRestaurant
     */

    private fun createFoh(
        rjd: RestaurantJsonDto,
        tsa: TableAssignmentService,
        type: RestaurantType,
        menu: Menu,
        pantry: Pantry,
        kitchen: Kitchen,
        rb: ReservationBook
    ): FrontOfTheHouse {
        // create foh: deliveryDesk, deliveryService drivers, waiterAssignmentService, fohSevices
        val deliveryDrivers: MutableList<DeliveryDriver> = mutableListOf()
        var driverCounter = rjd.deliveryDrivers
        while (driverCounter > 0) {
            val driver = DeliveryDriver(rjd.id)
            deliveryDrivers.add(driver)
            driverCounter--
        }
        DeliveryService.setAllDrivers(deliveryDrivers)
        val deliveryDesk = DeliveryDesk(deliveryDrivers, rjd.id)

        val waitstaff: MutableList<Waiter> = mutableListOf()
        var waiterCounter = rjd.waitstaff
        while (waiterCounter > 0) {
            val waiter = Waiter()
            waitstaff.add(waiter)
            waiterCounter--
        }
        val waiterAssignmentService = WaiterAssignmentService(waitstaff)

        val fohServices = FohServices(
            SeatingService(tsa, waiterAssignmentService, rb),
            OrderingService(waiterAssignmentService, type),
            ServingService(waiterAssignmentService, deliveryDesk, type),
            DiningService(),
            EscortingService(waiterAssignmentService),
            RatingService()
        )

        val subunit = SubUnits(rjd.id, menu, pantry, kitchen)

        return FrontOfTheHouse(
            subunit,
            tsa,
            rb,
            waiterAssignmentService,
            fohServices,
            deliveryDesk
        )
    }

    private fun createData(
        rjd: RestaurantJsonDto,
        tables: MutableList<Table>,
        recipes: MutableList<Recipe>,
        type: RestaurantType
    ): RestaurantData? {
        if (checkUniqueDishNames(recipes) && checkUniqueTableIds(tables) &&
            checkOpeningHours(rjd.openingTickStart, rjd.openingTickEnd)
        ) {
            val seats: MutableMap<TableType, Int> = mutableMapOf<TableType, Int>()
            var totalSeats = 0

            for (t in tables) {
                if (seats.containsKey(t.type)) {
                    seats.replace(t.type, checkNotNull(seats[t.type]) + t.size)
                } else {
                    seats[t.type] = t.size
                }
                totalSeats += t.size
            }

            return RestaurantData(
                rjd.id, type, rjd.openingTickStart, rjd.openingTickEnd,
                recipes, seats, rjd.deliveryDrivers, rjd.event,
                totalSeats, mutableMapOf<Int, Int>()
            )
        }
        return null
    }

    private fun resolveRecipes(rjd: RestaurantJsonDto, type: RestaurantType): MutableList<Recipe>? {
        val idDto: MutableList<Int> = rjd.recipes
        val explicitRecipes = mutableListOf<Recipe>()

        // 1. Resolve explicitly declared recipes
        for (recipeId in idDto) {
            val recipe: Recipe? = model.recipe(recipeId)
            if (recipe != null) {
                explicitRecipes.add(recipe)
            } else {
                return null
            }
        }

        val explicitDishNames = explicitRecipes.map { it.getDishName() }.toSet()

        // 2. Fetch default basic recipes for this restaurant type[cite: 2]
        // Note: You must add `allRecipes()` to ParsedModel returning all parsed Recipes.
        val defaultBasicRecipes = model.allRecipes().filter {
            it.isBasicFor(type) && it.getDishName() !in explicitDishNames
        }

        // 3. Combine explicit overrides and missing basic dishes
        val finalRecipes = explicitRecipes.toMutableList()
        finalRecipes.addAll(defaultBasicRecipes)
        return finalRecipes
    }

    private fun serialiseCookCounts(rjd: RestaurantJsonDto): Map<CookType, Int>? {
        var returnValue = true
        val cooksDto: Map<String, Int> = rjd.kitchenStaff
        val cooks = mutableMapOf<CookType, Int>()
        for ((key, value) in cooksDto) {
            try {
                if (key == "EXEC" && value != 0 && value != 1) { returnValue = false }
                cooks[CookType.valueOf(key)] = value
            } catch (_: IllegalArgumentException) {
                returnValue = false
            }
        }
        return if (returnValue) cooks else null
    }

    private fun serialiseTables(rjd: RestaurantJsonDto): MutableList<Table>? {
        val tablesDto: MutableList<TableDto> = rjd.tables
        val tables = mutableListOf<Table>()

        for ((id, type, size) in tablesDto) {
            try {
                val currentTable = Table(id, size, TableType.valueOf(type))
                tables.add(currentTable)
            } catch (_: IllegalArgumentException) {
                return null
            }
        }
        return tables
    }

// helper functions for serialiseRestaurant

    private fun checkUniqueDishNames(recipes: MutableList<Recipe>): Boolean {
        for (recipe in recipes) {
            var count = 0
            val name: String = recipe.getDishName()
            for (r in recipes) {
                if (name == r.getDishName()) count++
            }
            if (count > 1) return false
        }
        return true
    }

    private fun checkUniqueTableIds(tables: MutableList<Table>): Boolean {
        for (table in tables) {
            var count = 0
            val tableId: Int = table.id
            for (t in tables) {
                if (tableId == t.id) count++
            }
            if (count > 1) return false
        }
        return true
    }

    private fun checkOpeningHours(openingTick: Int, closingTick: Int): Boolean {
        return openingTick < closingTick
    }

    private fun checkAtLeastOneOfEach(rjd: RestaurantJsonDto): Boolean {
        // as long as the restaurant inherits its type's basic dishes.
        return rjd.kitchenStaff.values.sum() > 0 &&
            rjd.waitstaff > 0 && rjd.tables.isNotEmpty()
    }

    // helper functions for validateFileScope
    private fun checkBasicDishCoverage(dishes: Set<String>): Boolean {
        return dishes.isNotEmpty()
    }

    private fun checkAtLeastOneRestaurant(): Boolean {
        return model.allRestaurants().isNotEmpty()
    }
}
