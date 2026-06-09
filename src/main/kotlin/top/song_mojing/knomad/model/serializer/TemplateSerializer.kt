package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.*

/**
 * 模板字符串序列化器
 */
object TemplateSerializer : KSerializer<Template> {
    private val PLACEHOLDER_PATTERN = Regex("""[$][{][{]([a-zA-Z@]+)[.]([a-zA-Z][a-zA-Z0-9_]*)[}][}]""")
    private val STRICT_PATTERN = Regex("""^[{][{]([a-zA-Z@]+)[.]([a-zA-Z][a-zA-Z0-9_]*)[}][}]$""")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TemplateSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Template {
        return parseTemplateString(decoder.decodeString().trim())
    }

    fun parseTemplateString(input: String): Template {
        val strictMatch = STRICT_PATTERN.find(input)
        if (strictMatch != null) {
            val key = normalizeKey(strictMatch.groupValues[1])
            return Struct(key = key, value = strictMatch.groupValues[2])
        }
        
        val items = mutableListOf<ValueItem>()
        var lastIndex = 0
        
        PLACEHOLDER_PATTERN.findAll(input).forEach { match ->
            val prefixStart = maxOf(0, match.range.first - 10)
            val prefix = input.substring(prefixStart, match.range.first)
            if (isEscaped(prefix)) {
                return@forEach
            }
            
            if (match.range.first > lastIndex) {
                items.add(StringValue(input.substring(lastIndex, match.range.first)))
            }
            
            val key = normalizeKey(match.groupValues[1])
            items.add(Placeholder(key = key, value = match.groupValues[2]))
            lastIndex = match.range.last + 1
        }
        
        if (lastIndex < input.length) {
            items.add(StringValue(input.substring(lastIndex)))
        }
        
        return if (items.isEmpty()) {
            StringTemplate(listOf(StringValue(input)))
        } else {
            StringTemplate(items)
        }
    }
    
    private fun normalizeKey(key: String): String {
        return if (key.startsWith("knomad@")) {
            key
        } else if (key.contains("@")) {
            "knomad@$key"
        } else {
            key
        }
    }
    
    private fun isEscaped(prefix: String): Boolean {
        var count = 0
        for (i in prefix.length - 1 downTo 0) {
            if (prefix[i] == '\\') {
                count++
            } else {
                break
            }
        }
        return count % 2 == 1
    }

    override fun serialize(encoder: Encoder, value: Template) {
        val str = when (value) {
            is Struct -> "{{${
                if (!value.key.startsWith("knomad@")) "knomad@"
                else ""
            }${value.key}.${value.value}}}"
            is StringTemplate -> {
                value.value.joinToString("") { item ->
                    when (item) {
                        is StringValue -> item.value
                        is Placeholder -> "\${{${
                                if (!item.key.startsWith("knomad@")) "knomad@"
                                else ""
                        }${item.key}.${item.value}}}"
                    }
                }
            }
        }
        encoder.encodeString(str)
    }
}