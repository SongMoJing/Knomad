package top.song_mojing.knomad.parser.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.uri.URIFetcher
import java.io.ByteArrayInputStream
import java.net.URI

object YamlValidator {

    private val yamlMapper = YAMLMapper()
    private val jsonMapper = ObjectMapper()

    private const val MEMORY_SCHEMA_URI = "https://knomad.song-mojing.top/schema.json"

    fun validate(schemaContent: String, userYamlContent: String): List<ValidatorException> {
        val exceptions = mutableListOf<ValidatorException>()
        val schemaNode = try {
            yamlMapper.readTree(schemaContent)
        } catch (e: Exception) {
            exceptions.add(ValidatorException.InvalidSchemaException(e.localizedMessage))
            return exceptions
        }
        val userNode = try {
            yamlMapper.readTree(userYamlContent)
        } catch (e: Exception) {
            exceptions.add(ValidatorException.InvalidYamlException(e.localizedMessage))
            return exceptions
        }
        try {
            if (schemaNode is ObjectNode) {
                schemaNode.remove($$"$schema")
                schemaNode.put($$"$id", MEMORY_SCHEMA_URI)
            }
            val cleanJsonSchemaBytes = jsonMapper.writeValueAsBytes(schemaNode)
            val memoryFetcher = URIFetcher { uri ->
                if (uri.toString().startsWith(MEMORY_SCHEMA_URI)) {
                    ByteArrayInputStream(cleanJsonSchemaBytes)
                } else {
                    ByteArrayInputStream("{}".toByteArray())
                }
            }
            val config = SchemaValidatorsConfig().apply {
                isHandleNullableField = true
            }
            val factory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7))
                .uriFetcher(memoryFetcher, "https")
                .build()
            val jsonSchema = factory.getSchema(URI(MEMORY_SCHEMA_URI), schemaNode, config)
            val validationErrors = jsonSchema.validate(userNode)
            validationErrors.forEach { error ->
                exceptions.add(ValidatorException.InvalidGrammarException(error.path, error.message))
            }
        } catch (e: Exception) {
            e.message?.let { exceptions.add(ValidatorException.InvalidYamlException(it)) }
        }
        return exceptions
    }
}

sealed class ValidatorException(message: String) : Exception(message) {
    /**
     * 输入的 YAML 格式错误
     */
    open class InvalidYamlException(message: String) : ValidatorException(message)

    /**
     * 输入的 JSON Schema 错误
     */
    open class InvalidSchemaException(message: String) : ValidatorException(message)

    /**
     * 输入的 YAML 无法被 JSON Schema 验证
     */
    open class InvalidGrammarException(
        path: String,
        message: String
    ) : ValidatorException(message)
}
