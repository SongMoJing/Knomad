package top.song_mojing.knomad.model.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.KnomadType
import top.song_mojing.knomad.model.TemplateString
import kotlin.text.get

object TemplateStringSerializer : KSerializer<TemplateString> {

    private val CONTAIN_PLACEHOLDER = Regex("""(?<!\\)\$\{\{(?:knomad@)?(?<key>[a-zA-Z]+)\.(?<value>[a-zA-Z][a-zA-Z0-9_]+)}}""")
    private val STRICT_PLACEHOLDER = Regex("""^(?<!\\)\{\{(?:knomad@)?(?<key>[a-zA-Z]+)\.(?<value>[a-zA-Z][a-zA-Z0-9_]+)}}$""")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TemplateStringSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TemplateString {
        return parseTemplateString(decoder.decodeString().trim())
    }

    fun parseTemplateString(input: String): TemplateString {
        val structMatch = STRICT_PLACEHOLDER.find(input)
        if (structMatch != null) {
            return TemplateString.Struct(KnomadType.Custom(input.substring(2, input.length - 2)))
        }
        val items = mutableListOf<TemplateString.StringTemplate.ValueItem>()
        var lastIndex = 0
        CONTAIN_PLACEHOLDER.findAll(input).forEach { match ->
            if (match.range.first > lastIndex) {
                items.add(TemplateString.StringTemplate.ValueItem.StringValue(input.substring(lastIndex, match.range.first)))
            }
            items.add(
                TemplateString.StringTemplate.ValueItem.Placeholder(
                key = match.groups["key"]!!.value,
                value = match.groups["value"]!!.value
            ))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < input.length) {
            items.add(TemplateString.StringTemplate.ValueItem.StringValue(input.substring(lastIndex)))
        }
        return TemplateString.StringTemplate(items)
    }

    override fun serialize(encoder: Encoder, value: TemplateString) {
        val str = when (value) {
            is TemplateString.Struct -> "{{${
                if (!value.struct.typeName.startsWith("knomad@")) "knomad@"
                else ""
            }${value.struct.typeName}}}"
            is TemplateString.StringTemplate -> {
                value.value.joinToString("") { item ->
                    when (item) {
                        is TemplateString.StringTemplate.ValueItem.StringValue -> item.value
                        is TemplateString.StringTemplate.ValueItem.Placeholder -> $$"${{$${
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