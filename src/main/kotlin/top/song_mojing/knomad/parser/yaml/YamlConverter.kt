package top.song_mojing.knomad.parser.yaml

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.serialize.*
import top.song_mojing.knomad.model.serializer.NumberWrapper
import top.song_mojing.knomad.model.serializer.TemplateSerializer as TemplateSerializerObj

object YamlConverter {

    fun toKnomadConfig(raw: Any?): KnomadConfigStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("YAML 根节点必须是 Map，实际: ${raw?.javaClass?.name}")
        return KnomadConfigStruct(
            version = map.string("version"),
            baseUrl = map.string("baseUrl"),
            variables = map.mapValue("variables") { toVariableStruct(it) },
            endpoints = map.mapValue("endpoints") { toEndpointStruct(it) }
        )
    }

    private fun toVariableStruct(raw: Any?): VariableStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("Variable 必须是 Map")
        return VariableStruct(
            type = toKnomadType(map.string("type")),
            required = map.boolean("required"),
            description = map.stringOrNull("description")
        )
    }

    private fun toEndpointStruct(raw: Any?): EndpointStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("Endpoint 必须是 Map")
        return EndpointStruct(
            path = map.string("path"),
            method = toHttpMethod(map.string("method")),
            request = toRequestConfigStruct(map["request"]),
            response = map.listValue("response") { toResponseConfigStruct(it) }
        )
    }

    private fun toRequestConfigStruct(raw: Any?): RequestConfigStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("RequestConfig 必须是 Map")
        return RequestConfigStruct(
            query = map.mapValueOrNull("query") { toStringTemplate(it) },
            headers = map.mapValueOrNull("headers") { toStringTemplate(it) },
            body = map["body"]?.let { toTonItem(it) as? TonObject }
        )
    }

    private fun toResponseConfigStruct(raw: Any?): ResponseConfigStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("ResponseConfig 必须是 Map")
        return ResponseConfigStruct(
            httpCode = toHttpStatus(map.string("httpCode")),
            type = toMimeType(map.string("type")),
            values = map.listValue("values") { toResponseValueStruct(it) }
        )
    }

    private fun toResponseValueStruct(raw: Any?): ResponseValueStruct {
        val map = raw as? Map<*, *> ?: throw YamlConverterException("ResponseValue 必须是 Map")
        return ResponseValueStruct(
            name = map.string("name"),
            type = toKnomadType(map.string("type")),
            path = map.string("path")
        )
    }

    private fun toHttpMethod(raw: String): HttpMethod {
        return HttpMethod.entries.find { it.name.equals(raw, ignoreCase = true) }
            ?: throw YamlConverterException("未知的 HTTP 方法: $raw")
    }

    private fun toKnomadType(raw: String): KnomadType {
        return when (raw.lowercase()) {
            "string" -> KnomadType.String()
            "boolean" -> KnomadType.Boolean()
            "number" -> KnomadType.Number()
            "list" -> KnomadType.List()
            "map" -> KnomadType.Map()
            else -> throw YamlConverterException("未知的 KnomadType: $raw")
        }
    }

    private fun toHttpStatus(raw: String): HttpStatus {
        val split = raw.split(":", limit = 2)
        return when (split[0].trim().lowercase()) {
            "code" -> HttpStatus.Code(split[1].trim().toInt())
            "type" -> when (split[1].trim().lowercase()) {
                "success" -> HttpStatus.Status.Success
                "failed", "failure" -> HttpStatus.Status.Failure
                else -> throw YamlConverterException("无效的 HttpStatus type: $raw")
            }
            else -> throw YamlConverterException("无效的 HttpStatus: $raw")
        }
    }

    private fun toMimeType(raw: String): MimeType {
        val split = raw.split("/", limit = 2)
        if (split.size != 2) throw YamlConverterException("无效的 MimeType: $raw")
        return MimeType(type = split[0].trim(), subtype = split[1].trim())
    }

    private fun toStringTemplate(raw: Any?): StringTemplate {
        val str = raw?.toString() ?: ""
        val template = TemplateSerializerObj.parseTemplateString(str)
        return when (template) {
            is StringTemplate -> template
            is Struct -> StringTemplate(listOf(Placeholder(template.key, template.value)))
        }
    }

    internal fun toTonItem(raw: Any?): TonItem {
        return when (raw) {
            null -> TonNull()
            is Map<*, *> -> {
                val map = mutableMapOf<String, TonItem>()
                for ((k, v) in raw) {
                    map[k.toString()] = toTonItem(v)
                }
                TonObject(map)
            }
            is List<*> -> TonArray(raw.map { toTonItem(it) })
            is Boolean -> TonBoolean(raw)
            is Number -> TonNumber(NumberWrapper(raw))
            is String -> {
                if (raw.contains("{{")) {
                    when (val parsed = TemplateSerializerObj.parseTemplateString(raw)) {
                        is StringTemplate -> TonString(parsed)
                        is Struct -> TonTemplate(parsed)
                    }
                } else {
                    raw.toBooleanStrictOrNull()?.let { return TonBoolean(it) }
                    raw.toLongOrNull()?.let { return TonNumber(NumberWrapper(it)) }
                    raw.toDoubleOrNull()?.let { return TonNumber(NumberWrapper(it)) }
                    TonString(raw.toTonTemplate)
                }
            }
            else -> TonString(raw.toString().toTonTemplate)
        }
    }

    private fun Map<*, *>.string(key: String): String {
        return this[key]?.toString() ?: throw YamlConverterException("缺少必填字段: $key")
    }

    private fun Map<*, *>.stringOrNull(key: String): String? {
        return this[key]?.toString()
    }

    private fun Map<*, *>.boolean(key: String): Boolean {
        return when (val v = this[key]) {
            is Boolean -> v
            is String -> v.toBooleanStrictOrNull() ?: throw YamlConverterException("字段 $key 不是合法布尔值: $v")
            else -> throw YamlConverterException("字段 $key 不是合法布尔值: $v")
        }
    }

    private fun <V> Map<*, *>.mapValue(key: String, transform: (Any?) -> V): Map<String, V> {
        val raw = this[key] as? Map<*, *> ?: throw YamlConverterException("字段 $key 必须是 Map")
        return raw.entries.associate { (k, v) -> k.toString() to transform(v) }
    }

    private fun <V> Map<*, *>.mapValueOrNull(key: String, transform: (Any?) -> V): Map<String, V>? {
        val raw = this[key] ?: return null
        val map = raw as? Map<*, *> ?: throw YamlConverterException("字段 $key 必须是 Map")
        return map.entries.associate { (k, v) -> k.toString() to transform(v) }
    }

    private fun <V> Map<*, *>.listValue(key: String, transform: (Any?) -> V): List<V> {
        val raw = this[key] as? List<*> ?: throw YamlConverterException("字段 $key 必须是 List")
        return raw.map { transform(it) }
    }
}

private val String.toTonTemplate: StringTemplate
    get() = StringTemplate(listOf(StringValue(this)))

class YamlConverterException(message: String) : RuntimeException(message)