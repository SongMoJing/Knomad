package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.Template
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
            return Template.Struct(
                key = structMatch.groups["key"]!!.value,
                value = structMatch.groups["value"]!!.value
            )
        }
        val items = mutableListOf<Template.StringTemplate.ValueItem>()
        var lastIndex = 0
        CONTAIN_PLACEHOLDER.findAll(input).forEach { match ->
            if (match.range.first > lastIndex) {
                items.add(Template.StringTemplate.ValueItem.StringValue(input.substring(lastIndex, match.range.first)))
            }
            items.add(
                Template.StringTemplate.ValueItem.Placeholder(
                key = match.groups["key"]!!.value,
                value = match.groups["value"]!!.value
            ))
            lastIndex = match.range.last + 1
        }
        if (lastIndex < input.length) {
            items.add(Template.StringTemplate.ValueItem.StringValue(input.substring(lastIndex)))
        }
        return Template.StringTemplate(items)
    }

    override fun serialize(encoder: Encoder, value: Template) {
        val str = when (value) {
            is Template.Struct -> "{{${
                if (!value.key.startsWith("knomad@")) "knomad@"
                else ""
            }${value.value}}}"
            is Template.StringTemplate -> {
                value.value.joinToString("") { item ->
                    when (item) {
                        is Template.StringTemplate.ValueItem.StringValue -> item.value
                        is Template.StringTemplate.ValueItem.Placeholder -> $$"${{$${
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