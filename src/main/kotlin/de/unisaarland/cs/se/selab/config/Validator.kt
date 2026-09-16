package de.unisaarland.cs.se.selab.config

import com.github.erosb.jsonsKema.JsonParser
import com.github.erosb.jsonsKema.SchemaLoader
import com.github.erosb.jsonsKema.Validator as JsonSchemaValidator
import java.io.File
import java.io.IOException

/** Validates a configuration file against its JSON schema before any parser reads it. */
class Validator {

    /** Whether the file at [path] is syntactically valid JSON and satisfies the schema at [schemaPath]. */
    fun validateFile(path: String, schemaPath: String): Boolean {
        val text = readFile(path) ?: return false
        val instance = parseJson(text) ?: return false
        val schema = loadSchema(schemaPath) ?: return false
        return JsonSchemaValidator.forSchema(schema).validate(instance) == null
    }

    private fun readFile(path: String): String? = try {
        File(path).readText()
    } catch (_: IOException) {
        null
    }

    private fun parseJson(text: String) = try {
        JsonParser(text)()
    } catch (_: RuntimeException) {
        null
    }

    private fun loadSchema(schemaPath: String) = try {
        SchemaLoader.forURL("classpath:$schemaPath").load()
    } catch (_: RuntimeException) {
        null
    }
}
