package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.KnomadType

object KnomadTypeSerializer : KSerializer<KnomadType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("KnomadType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KnomadType {
        val typeStr = decoder.decodeString().trim()
        return when (typeStr.lowercase()) {
            "string" -> KnomadType.String()
            "boolean" -> KnomadType.Boolean()
            "number" -> KnomadType.Number()
            "list" -> KnomadType.List()
            "map" -> KnomadType.Map()
            else -> throw SerializationException("Unknown type: $typeStr")
        }
    }

    override fun serialize(encoder: Encoder, value: KnomadType) {
        when (value) {
            is KnomadType.List -> encoder.encodeString("List")
            is KnomadType.Map -> encoder.encodeString("Map")
            is KnomadType.String -> encoder.encodeString("String")
            is KnomadType.Boolean -> encoder.encodeString("Bool")
            is KnomadType.Number -> encoder.encodeString("Number")
        }
    }
}