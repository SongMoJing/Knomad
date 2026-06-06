package top.song_mojing.knomad.parser.yaml

import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.model.KnomadConfig
import java.io.File

object YamlParser {
    fun parser(
        yamlFile: File,
        schemaFile: File
    ): KnomadConfig {
        val yamlContent = yamlFile.readText()
        val schemaContent = schemaFile.readText()
        return parser(yamlContent, schemaContent)
    }

    fun parser(
        yamlFile: File
    ): KnomadConfig {
        val schemaContent = YamlParser::class.java.getResource("/schema.yaml")?.readText()
            ?: throw Exception("schema.yaml not found")
        return parser(yamlFile.readText(), schemaContent)
    }

    fun parser(
        yamlContent: String,
        schemaContent: String
    ): KnomadConfig {
        val exceptions = YamlValidator.validate(schemaContent, yamlContent)
        if (exceptions.isNotEmpty()) {
            throw YamlParserException(exceptions)
        }
        return Yaml.decodeFromString(KnomadConfig.serializer(), yamlContent)
    }
}

open class YamlParserException(val validatorExceptions: List<ValidatorException>) : Exception("YamlParserException")
