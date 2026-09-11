package de.unisaarland.cs.se.selab.config.scenarioParser

import de.unisaarland.cs.se.selab.config.ParsedModel
import de.unisaarland.cs.se.selab.config.Validator
import de.unisaarland.cs.se.selab.incident.IncidentType
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

private const val SCHEMA_PATH = "/schema/scenario.schema"

private data class UnavailabilityWindow(val ingredient: String, val from: Int, val to: Int)

/** Turns the scenario file into customer groups and incidents and registers them in the model. */
class ScenarioParser(
    private val model: ParsedModel,
    private val validator: Validator = Validator(),
) {

    private val json = Json { ignoreUnknownKeys = false }
    private val groupSerialiser = CustomerGroupSerialiser(model)
    private val incidentSerialiser = IncidentSerialiser(model)
    private val unavailabilityWindows = mutableListOf<UnavailabilityWindow>()

    /** Parses and validates the scenario file at [path], registering its contents in the model. */
    fun parse(path: String): Boolean =
        validator.validateFile(path, SCHEMA_PATH) && readEntities(path) && validateFileScope()

    private fun readEntities(path: String): Boolean {
        val file = decode(path) ?: return false
        return readCustomerGroups(file.customerGroups) && readIncidents(file.incidents)
    }

    private fun validateFileScope(): Boolean = noUnavailabilityOverlap()

    private fun decode(path: String): ScenarioFileDto? = try {
        json.decodeFromString<ScenarioFileDto>(File(path).readText())
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IOException) {
        null
    }

    private fun readCustomerGroups(dtos: List<CustomerGroupJsonDto>): Boolean {
        for (dto in dtos) {
            val group = groupSerialiser.serialise(dto) ?: return false
            if (!model.registerCustomerGroup(group)) return false
        }
        return true
    }

    private fun readIncidents(dtos: List<IncidentJsonDto>): Boolean {
        for (dto in dtos) {
            val incident = incidentSerialiser.serialise(dto) ?: return false
            if (!model.registerIncident(incident)) return false
            recordUnavailabilityWindow(dto)
        }
        return true
    }

    private fun recordUnavailabilityWindow(dto: IncidentJsonDto) {
        if (dto.type != IncidentType.UNAVAILABLE.name) return
        val ingredient = dto.ingredient ?: return
        val duration = dto.duration ?: return
        unavailabilityWindows.add(
            UnavailabilityWindow(ingredient, dto.evening, dto.evening + duration - 1),
        )
    }

    private fun noUnavailabilityOverlap(): Boolean =
        unavailabilityWindows
            .groupBy { it.ingredient }
            .values
            .none { windowsOverlap(it) }

    private fun windowsOverlap(windows: List<UnavailabilityWindow>): Boolean =
        windows
            .sortedBy { it.from }
            .zipWithNext()
            .any { (earlier, later) -> earlier.to >= later.from }
}
