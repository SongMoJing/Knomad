package top.song_mojing.knomad.model.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.KnomadType

object BaseTypeSerializer : KSerializer<KnomadType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("KnomadType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): KnomadType {
        val typeStr = decoder.decodeString().trim()
        if (typeStr.startsWith("List:")) {
            val innerTypeName = typeStr.substringAfter("List:")
            return KnomadType.List(parseBaseType(innerTypeName))
        }
        if (typeStr.startsWith("Map:")) {
            val parts = typeStr.substringAfter("Map:").split(",")
            if (parts.size == 2) {
                return KnomadType.Object(parseBaseType(parts[0]), parseBaseType(parts[1]))
            }
        }
        return parseBaseType(typeStr)
    }

    private fun parseBaseType(name: String): KnomadType {
        return when (name.lowercase()) {
            "string" -> KnomadType.String()
            "bool" -> KnomadType.Bool()
            "int" -> KnomadType.Int()
            "float" -> KnomadType.Float()
            else -> {
                KnomadType.Other(name)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: KnomadType) {
        when (value) {
            is KnomadType.List -> encoder.encodeString("List:${value.t1}")
            is KnomadType.Object -> encoder.encodeString("Map:${value.t1},${value.t2}")
            is KnomadType.String -> encoder.encodeString("String")
            is KnomadType.Bool -> encoder.encodeString("Bool")
            is KnomadType.Int -> encoder.encodeString("Int")
            is KnomadType.Float -> encoder.encodeString("Float")
            is KnomadType.Other -> encoder.encodeString(value.key)
        }
    }
}