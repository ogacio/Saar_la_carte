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
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantData
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.TableType
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

    protected override fun readEntities(path: String): Boolean {
        var returnValue = true
        val text = File(path).readText()
        val fileDto: RestaurantFileDto = try {
            Json.decodeFromString(text)
        } catch (_: SerializationException) {
            return false
        }
        for (restaurantDto in fileDto.restaurants) {
            val restaurant = serialiseRestaurant(restaurantDto)
            if (restaurant == null || !model.registerRestaurant(restaurant)) {
                returnValue = false
                break
            }
        }
        return returnValue
    }

    protected override fun validateFileScope(): Boolean {
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

    /**
     * helper function for readEntities, creates Restaurant objects
     */
    private fun serialiseRestaurant(rjd: RestaurantJsonDto): Restaurant? {
        try {
            val type = RestaurantType.valueOf(rjd.type)
            if (checkAtLeastOneOfEach(rjd)) {
                val foh: FrontOfTheHouse?
                val pantry = Pantry()
                val kitchen: Kitchen?
                val menu: Menu?

                // create kitchen, foh and menu
                val kitchenStaff = serialiseCookCounts(rjd)
                val tables = serialiseTables(rjd)
                val recipes = resolveRecipes(rjd)

                if (kitchenStaff != null && tables != null && recipes != null) {
                    // create kitchen: roaster, tableAssignmentService, reservationBook
                    val roaster = CookRoaster(kitchenStaff)
                    val tableAssignmentService = TableAssignmentService(tables)
                    val reservationBook = ReservationBook(tableAssignmentService)
                    kitchen = Kitchen(roaster, pantry, mutableListOf<Order>(), reservationBook)

                    // create menu
                    menu = Menu(recipes, pantry, kitchen)

                    // create foh: deliveryDesk, waiterAssignmentService, fohSevices
                    val deliveryDrivers: MutableList<DeliveryDriver> = mutableListOf()
                    var driverCounter = rjd.deliveryDrivers
                    while (driverCounter > 0) {
                        val driver = DeliveryDriver(rjd.id)
                        deliveryDrivers.add(driver)
                        driverCounter--
                    }
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
                        SeatingService(tableAssignmentService, waiterAssignmentService, reservationBook),
                        OrderingService(waiterAssignmentService, type),
                        ServingService(waiterAssignmentService, deliveryDesk, type),
                        DiningService(),
                        EscortingService(waiterAssignmentService),
                        RatingService()
                    )

                    val subunit = SubUnits(rjd.id, menu, pantry, kitchen)

                    foh = FrontOfTheHouse(
                        subunit, tableAssignmentService, reservationBook,
                        waiterAssignmentService, fohServices, deliveryDesk
                    )
                } else {
                    kitchen = null
                    foh = null
                    menu = null
                }

                val data = createData(rjd)

                if (foh != null && kitchen != null && menu != null && data != null) {
                    kitchen.roaster.initialiseCooks()
                    return Restaurant(
                        rjd.name, rjd.event, rjd.positiveRatings,
                        rjd.negativeRatings, foh, kitchen, pantry, menu, data
                    )
                }
            }
        } catch (_: IllegalArgumentException) { }
        return null
    }

    /**
     * creates RestaurantData
     */
    private fun createData(rjd: RestaurantJsonDto): RestaurantData? {
        var returnValue = true

        val tables = serialiseTables(rjd)
        val recipes = resolveRecipes(rjd)
        if (recipes != null && tables != null) {
            if (!(
                    checkUniqueDishNames(recipes) && checkUniqueTableIds(tables) &&
                        checkOpeningHours(rjd.openingTickStart, rjd.openingTickEnd)
                    )
            ) {
                returnValue = false
            }

            if (returnValue) {
                try {
                    val type = RestaurantType.valueOf(rjd.type)

                    val seats: MutableMap<TableType, Int> = mutableMapOf<TableType, Int>()
                    var totalSeats = 0

                    for (t in tables) {
                        if (seats.containsKey(t.type)) {
                            seats.replace(t.type, seats[ t.type ]!! + t.size)
                        } else {
                            seats[ t.type ] = t.size
                        }
                        totalSeats += t.size
                    }

                    return RestaurantData(
                        rjd.id, type, rjd.openingTickStart, rjd.openingTickEnd,
                        recipes, seats, rjd.deliveryDrivers, rjd.event,
                        totalSeats, mutableMapOf<Int, Int>()
                    )
                } catch (_: IllegalArgumentException) { }
            }
        }
        return null
    }

    private fun resolveRecipes(rjd: RestaurantJsonDto): MutableList<Recipe>? {
        val idDto: MutableList<Int> = rjd.recipes
        val recipes = mutableListOf<Recipe>()
        for (recipeId in idDto) {
            val recipe: Recipe? = model.recipe(recipeId)
            if (recipe != null) { recipes.add(recipe) } else { return null }
        }
        return recipes
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
            val name: String = recipe.dishName
            for (r in recipes) {
                if (name == r.dishName) count++
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
        return rjd.recipes.isNotEmpty() && rjd.kitchenStaff.isNotEmpty() && rjd.waitstaff > 0 && rjd.tables.isNotEmpty()
    }

// helper functions for validateFileScope
    private fun checkBasicDishCoverage(dishes: Set<String>): Boolean {
        return dishes.isNotEmpty()
    }

    private fun checkAtLeastOneRestaurant(): Boolean {
        return model.allRestaurants().isNotEmpty()
    }
}
