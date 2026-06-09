package top.song_mojing.knomad.parser.yaml

import org.yaml.snakeyaml.Yaml
import top.song_mojing.knomad.model.serialize.KnomadConfigStruct
import java.io.File

@Suppress("unused")
object YamlParser {

    private val yaml = Yaml()

    fun parser(
        yamlFile: File,
        schemaFile: File
    ): KnomadConfigStruct {
        val yamlContent = yamlFile.readText()
        val schemaContent = schemaFile.readText()
        return parser(yamlContent, schemaContent)
    }

    fun parser(
        yamlFile: File
    ): KnomadConfigStruct {
        val schemaContent = YamlParser::class.java.getResource("/schema.yaml")?.readText()
            ?: throw Exception("schema.yaml not found")
        return parser(yamlFile.readText(), schemaContent)
    }

    fun parser(
        yamlContent: String
    ): KnomadConfigStruct {
        val schemaContent = YamlParser::class.java.getResource("/schema.yaml")?.readText()
            ?: throw Exception("schema.yaml not found")
        return parser(yamlContent, schemaContent)
    }

    fun parser(
        yamlContent: String,
        schemaContent: String
    ): KnomadConfigStruct {
        val exceptions = YamlValidator.validate(schemaContent, yamlContent)
        if (exceptions.isNotEmpty()) {
            throw YamlParserException(exceptions)
        }
        val raw: Any? = yaml.load<Any>(yamlContent)
        return YamlConverter.toKnomadConfig(raw)
    }
}

open class YamlParserException(val validatorExceptions: List<ValidatorException>) : Exception("YamlParserException")