package de.unisaarland.cs.se.selab.config.scenarioParser

import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.incident.Incident
import de.unisaarland.cs.se.selab.incident.IncidentType
import de.unisaarland.cs.se.selab.incident.IngredientUnavailability
import de.unisaarland.cs.se.selab.incident.PackagingChange
import de.unisaarland.cs.se.selab.incident.RecipeChange
import de.unisaarland.cs.se.selab.incident.StaffChange
import de.unisaarland.cs.se.selab.kitchen.CookType
import de.unisaarland.cs.se.selab.shared_data.StaffType

/** Turns [IncidentJsonDto]s into incidents and applies the constraints the schema cannot express. */
class IncidentSerialiser(private val model: ParsedModel) {

    /** Serialises [dto] into its incident, or null if a referenced object does not exist. */
    fun serialise(dto: IncidentJsonDto): Incident? = when (typeOf(dto)) {
        IncidentType.STAFF -> serialiseStaffChange(dto)
        IncidentType.RECIPE -> serialiseRecipeChange(dto)
        IncidentType.PACKAGING -> serialisePackagingChange(dto)
        IncidentType.UNAVAILABLE -> serialiseIngredientUnavailability(dto)
        null -> null
    }

    private fun typeOf(dto: IncidentJsonDto): IncidentType? =
        IncidentType.entries.firstOrNull { it.name == dto.type }

    private fun serialiseStaffChange(dto: IncidentJsonDto): StaffChange? {
        val restaurantId = dto.restaurant ?: return null
        if (model.restaurant(restaurantId) == null) return null
        val staffType = dto.staffType?.let { StaffType.valueOf(it) } ?: return null
        val cookType = dto.cookType?.let { CookType.valueOf(it) }
        return StaffChange(
            id = dto.id,
            evening = dto.evening,
            restaurantId = restaurantId,
            number = dto.number ?: return null,
            staffType = staffType,
            cookType = cookType,
        )
    }

    private fun serialiseRecipeChange(dto: IncidentJsonDto): RecipeChange? {
        val ingredient = model.ingredient(dto.ingredient ?: return null) ?: return null
        return RecipeChange(
            id = dto.id,
            evening = dto.evening,
            ingredient = ingredient,
            adaptation = dto.adaptation ?: return null,
        )
    }

    private fun serialisePackagingChange(dto: IncidentJsonDto): PackagingChange? {
        val ingredient = model.ingredient(dto.ingredient ?: return null) ?: return null
        return PackagingChange(
            id = dto.id,
            evening = dto.evening,
            ingredient = ingredient,
            packagingVolume = dto.packagingVolume ?: return null,
        )
    }

    private fun serialiseIngredientUnavailability(dto: IncidentJsonDto): IngredientUnavailability? {
        val ingredient = model.ingredient(dto.ingredient ?: return null) ?: return null
        return IngredientUnavailability(
            id = dto.id,
            evening = dto.evening,
            ingredient = ingredient,
            duration = dto.duration ?: return null,
        )
    }
}
