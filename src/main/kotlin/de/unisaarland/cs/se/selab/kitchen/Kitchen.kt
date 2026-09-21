package de.unisaarland.cs.se.selab.kitchen
import de.unisaarland.cs.se.selab.logging.Logger
import de.unisaarland.cs.se.selab.logging.Logger.Kitchen.mealCooked
import de.unisaarland.cs.se.selab.sharedPackage.Ingredient
import de.unisaarland.cs.se.selab.sharedPackage.Meal
import de.unisaarland.cs.se.selab.sharedPackage.MealStatus
import de.unisaarland.cs.se.selab.sharedPackage.Menu
import de.unisaarland.cs.se.selab.sharedPackage.Order
import de.unisaarland.cs.se.selab.sharedPackage.Pantry
import de.unisaarland.cs.se.selab.sharedPackage.Recipe
import de.unisaarland.cs.se.selab.sharedPackage.RestaurantType
import de.unisaarland.cs.se.selab.sharedPackage.customers.CustomerGroup
import de.unisaarland.cs.se.selab.simulation.GlobalClock
import de.unisaarland.cs.se.selab.simulation.ReservationBook
import de.unisaarland.cs.se.selab.simulation.Supplier.resupply
import kotlin.comparisons.compareBy

private const val SEATS_PER_ESTIMATE = 10

/** The kitchen, responsible for calling every function that happens inside the package. */
class Kitchen(
    val roaster: CookRoaster,
    val pantry: Pantry,
    val queue: MutableList<Order>,
    val reservationBook: ReservationBook,
    val restaurantType: RestaurantType,
) {
    /** Called by foh, puts the order into the queue. */
    fun enqueue(o: Order) {
        queue.add(o)
    }

    /** One tick: assign the queued dishes, then finish what is done, then the summary line. */
    fun cook(): Int {
        // assigning first is what lets a 10 minute dish finish in the tick it was ordered.
        assignQueuedMeals()

        // already in ascending cook id, so the Meal Cooked lines come out in that order.
        val finishedBatches = roaster.finished()
        val finishedCount = reportFinishedBatches(finishedBatches)

        // the summary the kitchen
        Logger.Kitchen.kitchenStatus(
            pantry.getRestaurantId(),
            roaster.cooksWithAFullPan() + finishedBatches.size,
            roaster.mealsInPans() + finishedCount,
            finishedCount,
            servableMeals(),
        )
        return finishedCount
    }

    /** Dishes with smaller orderIds first, basic dishes first, then the lower recipe id;
     * a dish without a free cook stays queued. */
    private fun assignQueuedMeals() {
        for (order in queue.sortedBy { it.getId() }) {
            val recipes = order.getMeals()
                .filter { it.status == MealStatus.QUEUED }
                .map { it.recipe }
                .distinct()
                .sortedWith(
                    compareBy<Recipe>(
                        { !it.isBasicFor(restaurantType) },
                        { it.getId() },
                    ),
                )

            for (recipe in recipes) {
                if (!roaster.hasEligibleAndFree(recipe)) continue

                val batch = queue
                    .flatMap { it.getMeals() }
                    .filter {
                        it.status == MealStatus.QUEUED &&
                            it.recipe == recipe
                    }
                    .toMutableList()

                roaster.startCooking(batch)
            }
        }
    }

    /** one line per batch, with the ticks since the order the assignment named. */
    private fun reportFinishedBatches(finishedBatches: Map<Cook, List<Meal>>): Int {
        val ordersById = queue.associateBy { it.getId() }
        var count = 0
        for ((cook, meals) in finishedBatches) {
            meals.forEach { pantry.deleteFromReserved(it.recipe) }
            val sourceOrder = meals.mapNotNull { it.orderId }.minOrNull()?.let { ordersById[it] }
            if (sourceOrder != null) {
                mealCooked(
                    pantry.getRestaurantId(),
                    requireNotNull(cook.getId()),
                    meals.size,
                    meals.first().recipe.getDishName(),
                    sourceOrder.ticksSince(),
                )
            }
            count += meals.size
        }
        return count
    }

    /** Meals that are cooked and still waiting for a waiter, the last number of the status line. */
    private fun servableMeals(): Int =
        queue.sumOf { order -> order.getMeals().count { it.status == MealStatus.COOKED } }

    /** Called by Restaurant -> prepare, gets the supplies from Supplier into the Pantry. */
    fun planEvening(regulars: MutableList<CustomerGroup>, otherSeats: Int, menu: Menu) {
        pantry.checkDateAndCleanOut()
        val expected = getExpectedRecipes(regulars, otherSeats, menu)
        val totalRequired: MutableMap<Ingredient, Int> = mutableMapOf()
        for ((recipe, count) in expected) {
            for (ingredient in recipe.ingredients) {
                val current = ingredient.ingredient
                totalRequired[current] = (totalRequired[current] ?: 0) + count * ingredient.amount
            }
        }
        // "The cooks buy all ingredients of which there isn't enough available", so only the gap.
        val needed: MutableMap<Ingredient, Int> = mutableMapOf()
        for ((ingredient, concreteAmount) in totalRequired) {
            val inPantry = pantry.getTotalIngredients(ingredient)
            if (concreteAmount > inPantry) {
                needed[ingredient] = concreteAmount - inPantry
            }
        }
        resupply(pantry, needed)
    }

    /** The dishes the known groups will order, plus one per dish per ten other seats. */
    private fun getExpectedRecipes(
        regulars: MutableList<CustomerGroup>,
        otherSeats: Int,
        menu: Menu,
    ): MutableMap<Recipe, Int> {
        val reservers = regulars.toMutableList()
        reservers.addAll(reservationBook.expectedFor(GlobalClock.getEvening()))
        val expected: MutableMap<Recipe, Int> = mutableMapOf()
        for (r in reservers) {
            for ((recipe, amount) in r.expectedDishes(menu.getRecipes())) {
                expected[recipe] = (expected[recipe] ?: 0) + amount
            }
        }
        val casualEstimate = (otherSeats + SEATS_PER_ESTIMATE - 1) / SEATS_PER_ESTIMATE
        for (recipe in menu.getRecipes()) {
            expected[recipe] = (expected[recipe] ?: 0) + casualEstimate
        }
        return expected
    }

    /** F13: a dish is only orderable while some cook of the restaurant could cook it at all. */
    fun canCook(r: Recipe): Boolean = roaster.hasEligible(r)

    /** Called by incident, changes the cooks. */
    fun changeStaff(type: CookType, delta: Int) {
        roaster.changeStaff(type, delta)
    }

    /** Called at the end of the evening by Restaurant -> closeEvening(), resets everything. */
    fun closeEvening() {
        roaster.resetEvening()
        queue.clear()
    }
}
