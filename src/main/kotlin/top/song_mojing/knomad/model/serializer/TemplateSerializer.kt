package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.*
import kotlin.text.get

object TemplateSerializer : KSerializer<Template> {

    private val CONTAIN_PLACEHOLDER = Regex("""(?<!\\)\$\{\{(?:knomad@)?(?<key>[a-zA-Z]+)\.(?<value>[a-zA-Z][a-zA-Z0-9_]+)}}""")
    private val STRICT_PLACEHOLDER = Regex("""^(?<!\\)\{\{(?:knomad@)?(?<key>[a-zA-Z]+)\.(?<value>[a-zA-Z][a-zA-Z0-9_]+)}}$""")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TemplateSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Template {
        return parseTemplateString(decoder.decodeString().trim())
    }

    fun parseTemplateString(input: String): Template {
        val structMatch = STRICT_PLACEHOLDER.find(input)
        if (structMatch != null) {
            return Struct(
                key = structMatch.groups["key"]!!.value,
                value = structMatch.groups["value"]!!.value
            )
        }
        val items = mutableListOf<ValueItem>()
        var lastIndex = 0
        CONTAIN_PLACEHOLDER.findAll(input).forEach { match ->
            if (match.range.first > lastIndex) {
                items.add(StringValue(input.substring(lastIndex, match.range.first)))
            }
            items.add(
                Placeholder(
                key = match.groups["key"]!!.value,
                value = match.groups["value"]!!.value
            ))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < input.length) {
            items.add(StringValue(input.substring(lastIndex)))
        }
        return StringTemplate(items)
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
                        is Placeholder -> $$"${{$${
                                if (!item.key.startsWith("knomad@")) "knomad@"
                                else ""
                        }$${item.key}.$${item.value}}}"
                    }
                }
            }
        }
        encoder.encodeString(str)
    }
}