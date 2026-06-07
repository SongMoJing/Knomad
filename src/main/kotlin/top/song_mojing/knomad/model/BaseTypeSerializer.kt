package top.song_mojing.knomad.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BaseTypeSerializer : KSerializer<BaseType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BaseType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BaseType {
        val typeStr = decoder.decodeString().trim()
        if (typeStr.startsWith("List:")) {
            val innerTypeName = typeStr.substringAfter("List:")
            return BaseType.List(parseBaseType(innerTypeName))
        }
        if (typeStr.startsWith("Map:")) {
            val parts = typeStr.substringAfter("Map:").split(",")
            if (parts.size == 2) {
                return BaseType.Object(parseBaseType(parts[0]), parseBaseType(parts[1]))
            }
        }
        return parseBaseType(typeStr)
    }

    private fun parseBaseType(name: String): BaseType {
        return when (name.lowercase()) {
            "string" -> BaseType.String()
            "bool" -> BaseType.Bool()
            "int" -> BaseType.Int()
            "float" -> BaseType.Float()
            else -> {
                BaseType.Other(name)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: BaseType) {
        when (value) {
            is BaseType.List -> encoder.encodeString("List:${value.t1}")
            is BaseType.Object -> encoder.encodeString("Map:${value.t1},${value.t2}")
            is BaseType.String -> encoder.encodeString("String")
            is BaseType.Bool -> encoder.encodeString("Bool")
            is BaseType.Int -> encoder.encodeString("Int")
            is BaseType.Float -> encoder.encodeString("Float")
            is BaseType.Other -> encoder.encodeString(value.key)
        }
    }
}