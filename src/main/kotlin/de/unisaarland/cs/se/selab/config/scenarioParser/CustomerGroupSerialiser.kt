package de.unisaarland.cs.se.selab.config.scenarioParser

import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.customer.EventCustomerGroup
import de.unisaarland.cs.se.selab.shared_data.CasualCustomerGroup
import de.unisaarland.cs.se.selab.shared_data.Customer
import de.unisaarland.cs.se.selab.shared_data.CustomerGroup
import de.unisaarland.cs.se.selab.shared_data.FoodPreference
import de.unisaarland.cs.se.selab.shared_data.RegularCustomerGroup
import de.unisaarland.cs.se.selab.shared_data.RestaurantType
import de.unisaarland.cs.se.selab.shared_data.TableType
import de.unisaarland.cs.se.selab.simulation.ratings.RatingLikelihood
import kotlin.math.ceil

private const val COOKING_TICKS = 3
private const val DELIVERY_KM_PER_TICK = 5.0
private const val REGULAR = "REGULAR"
private const val CASUAL = "CASUAL"
private const val EVENT = "EVENT"

/** Turns [CustomerGroupJsonDto]s into customer groups, applying the constraints the schema can't. */
class CustomerGroupSerialiser(private val model: ParsedModel) {

    /** Serialises [dto] into its group, or null if any constraint fails. */
    fun serialise(dto: CustomerGroupJsonDto): CustomerGroup? {
        val preferences = serialiseFoodPreferences(dto.foodPreferences) ?: return null
        if (preferences.sumOf { it.size } > dto.size) return null
        return when (dto.type) {
            REGULAR -> serialiseRegular(dto, preferences)
            CASUAL -> serialiseCasual(dto, preferences)
            EVENT -> serialiseEvent(dto, preferences)
            else -> null
        }
    }

    private fun serialiseFoodPreferences(dtos: List<FoodPreferenceDto>): List<FoodPreference>? {
        val result = mutableListOf<FoodPreference>()
        for (dto in dtos) {
            result.add(serialiseFoodPreference(dto) ?: return null)
        }
        return result
    }

    private fun serialiseFoodPreference(dto: FoodPreferenceDto): FoodPreference? {
        val excluded = dto.excludedIngredients.orEmpty()
        val preferred = dto.preferredIngredients.orEmpty()
        val favourites = dto.favoriteDishes.orEmpty()
        if (!preferenceIsValid(excluded, preferred, favourites)) return null
        return FoodPreference(
            size = dto.size,
            excluded = resolveIngredients(excluded),
            preferred = resolveIngredients(preferred),
            favouriteDishNames = favourites,
        )
    }

    private fun preferenceIsValid(
        excluded: List<String>,
        preferred: List<String>,
        favourites: List<String>,
    ): Boolean =
        noExcludedPreferredOverlap(excluded, preferred) &&
            allReferencedNamesExist(excluded, preferred, favourites) &&
            noArrayIsTheFullSet(excluded, preferred, favourites)

    private fun noExcludedPreferredOverlap(excluded: List<String>, preferred: List<String>): Boolean =
        excluded.none { preferred.contains(it) }

    private fun allReferencedNamesExist(
        excluded: List<String>,
        preferred: List<String>,
        favourites: List<String>,
    ): Boolean =
        (excluded + preferred).all { model.ingredient(it) != null } &&
            favourites.all { model.allDishNames().contains(it) }

    private fun noArrayIsTheFullSet(
        excluded: List<String>,
        preferred: List<String>,
        favourites: List<String>,
    ): Boolean {
        val allIngredients = model.allIngredientNames()
        return excluded.toSet() != allIngredients &&
            preferred.toSet() != allIngredients &&
            favourites.toSet() != model.allDishNames()
    }

    private fun resolveIngredients(names: List<String>) =
        names.mapNotNull { model.ingredient(it) }.toSet()

    private fun serialiseRegular(
        dto: CustomerGroupJsonDto,
        preferences: List<FoodPreference>,
    ): CustomerGroup? {
        if (!regularFieldsAreValid(dto)) return null
        val restaurantId = dto.restaurant ?: return null
        val tableType = tableTypeOf(dto) ?: return null
        return RegularCustomerGroup(
            id = dto.id,
            groupSize = dto.size,
            tableType = tableType,
            visitingTick = dto.visitingTick,
            members = membersFor(dto.size, preferences),
            preferences = preferences,
            visitingStart = dto.visitingStart ?: 0,
            visitingPeriod = dto.visitingPeriod ?: 1,
            restaurantId = restaurantId,
        )
    }

    private fun regularFieldsAreValid(dto: CustomerGroupJsonDto): Boolean {
        val restaurantId = dto.restaurant ?: return false
        return dto.visitingStart != null &&
            dto.visitingPeriod != null &&
            tableTypeOf(dto) != null &&
            model.restaurant(restaurantId) != null
    }

    private fun serialiseCasual(
        dto: CustomerGroupJsonDto,
        preferences: List<FoodPreference>,
    ): CustomerGroup? {
        if (!casualFieldsAreValid(dto)) return null
        val tableType = tableTypeOf(dto) ?: return null
        return CasualCustomerGroup(
            id = dto.id,
            groupSize = dto.size,
            tableType = tableType,
            visitingTick = dto.visitingTick,
            members = membersFor(dto.size, preferences),
            preferences = preferences,
            restaurantTypes = restaurantTypesOf(dto).orEmpty(),
            visitingEvenings = dto.visitingEvenings.orEmpty(),
            deliveryDistance = dto.deliveryDistance ?: 0,
            ratingLikelihood = ratingLikelihoodOf(dto) ?: RatingLikelihood.NEVER,
        )
    }

    private fun casualFieldsAreValid(dto: CustomerGroupJsonDto): Boolean {
        val visitingEvenings = dto.visitingEvenings ?: return false
        if (visitingEvenings.any { it <= 0 }) return false
        if (restaurantTypesOf(dto) == null || ratingLikelihoodOf(dto) == null) return false
        val distance = dto.deliveryDistance ?: 0
        return distance <= 0 || deliveryOrderInPhase(dto.visitingTick, distance)
    }

    private fun serialiseEvent(
        dto: CustomerGroupJsonDto,
        preferences: List<FoodPreference>,
    ): EventCustomerGroup? {
        if (!eventFieldsAreValid(dto)) return null
        val tableType = tableTypeOf(dto) ?: return null
        return EventCustomerGroup(
            id = dto.id,
            groupSize = dto.size,
            tableType = tableType,
            visitingTick = dto.visitingTick,
            members = membersFor(dto.size, preferences),
            preferences = preferences,
            restaurantTypes = restaurantTypesOf(dto).orEmpty(),
            eventEvening = dto.eventEvening ?: 0,
            favouriteDishes = favouriteDishesOf(dto.favoriteDishes).orEmpty(),
        )
    }

    private fun eventFieldsAreValid(dto: CustomerGroupJsonDto): Boolean {
        val restaurantTypes = restaurantTypesOf(dto) ?: return false
        val favourites = favouriteDishesOf(dto.favoriteDishes) ?: return false
        if (dto.eventEvening == null) return false
        return favourites.keys.containsAll(restaurantTypes) &&
            favourites.all { (type, dish) -> model.basicDishesFor(type).contains(dish) }
    }

    private fun deliveryOrderInPhase(visitingTick: Int, distance: Int): Boolean =
        visitingTick - travelTicks(distance) - COOKING_TICKS >= 1

    private fun travelTicks(distance: Int): Int = ceil(distance / DELIVERY_KM_PER_TICK).toInt()

    private fun membersFor(size: Int, preferences: List<FoodPreference>): List<Customer> {
        val members = mutableListOf<Customer>()
        for (preference in preferences) {
            repeat(preference.size) { members.add(Customer(preference)) }
        }
        repeat(size - members.size) { members.add(Customer(null)) }
        return members
    }

    private fun tableTypeOf(dto: CustomerGroupJsonDto): TableType? {
        val name = dto.tableType ?: return TableType.COMMON
        return TableType.entries.firstOrNull { it.name == name }
    }

    private fun restaurantTypesOf(dto: CustomerGroupJsonDto): Set<RestaurantType>? {
        val names = dto.restaurantTypes ?: return null
        val types = names.map { name -> restaurantTypeOf(name) ?: return null }
        return types.toSet()
    }

    private fun restaurantTypeOf(name: String): RestaurantType? =
        RestaurantType.entries.firstOrNull { it.name == name }

    private fun ratingLikelihoodOf(dto: CustomerGroupJsonDto): RatingLikelihood? {
        val name = dto.ratingLikelihood ?: return null
        return RatingLikelihood.entries.firstOrNull { it.name == name }
    }

    private fun favouriteDishesOf(raw: Map<String, String>?): Map<RestaurantType, String>? {
        val entries = raw ?: return null
        val resolved = mutableMapOf<RestaurantType, String>()
        for ((name, dish) in entries) {
            resolved[restaurantTypeOf(name) ?: return null] = dish
        }
        return resolved
    }
}
