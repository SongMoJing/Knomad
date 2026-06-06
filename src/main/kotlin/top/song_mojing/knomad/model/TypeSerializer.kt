package top.song_mojing.knomad.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TypeSerializer : KSerializer<Type> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("top.song_mojing.knomad.model.Type", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Type {
        val typeStr = decoder.decodeString().trim()
        if (typeStr.startsWith("List:")) {
            val innerTypeName = typeStr.substringAfter("List:")
            return Type.List(parseBaseType(innerTypeName))
        }
        if (typeStr.startsWith("Map:")) {
            val parts = typeStr.substringAfter("Map:").split(",")
            if (parts.size == 2) {
                return Type.Object(parseBaseType(parts[0]), parseBaseType(parts[1]))
            }
        }
        return parseBaseType(typeStr)
    }

    private fun parseBaseType(name: String): Type {
        return when (name.lowercase()) {
            "string" -> Type.String()
            "bool" -> Type.Bool()
            "int" -> Type.Int()
            "float" -> Type.Float()
            else -> {
                Type.Other(name)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Type) {
        when (value) {
            is Type.List -> encoder.encodeString("List:${value.t1}")
            is Type.Object -> encoder.encodeString("Map:${value.t1},${value.t2}")
            is Type.String -> encoder.encodeString("String")
            is Type.Bool -> encoder.encodeString("Bool")
            is Type.Int -> encoder.encodeString("Int")
            is Type.Float -> encoder.encodeString("Float")
            is Type.Other -> encoder.encodeString(value.key)
        }
    }
}