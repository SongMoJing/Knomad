package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.StringTemplate

object StringTemplateSerializer : KSerializer<StringTemplate> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StringTemplate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringTemplate {
        val input = decoder.decodeString()
        val template = TemplateSerializer.parseTemplateString(input)
        if (template is StringTemplate) {
            return template
        }
        throw SerializationException("Expected a StringTemplate but got ${template::class.simpleName} for input: $input")
    }

    override fun serialize(encoder: Encoder, value: StringTemplate) {
        TemplateSerializer.serialize(encoder, value)
    }
}