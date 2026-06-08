package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = NumberSerializer::class)
class NumberWrapper(val value: Number)

object NumberSerializer : KSerializer<NumberWrapper> {
    override val descriptor = PrimitiveSerialDescriptor("NumberWrapper", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: NumberWrapper) {
        encoder.encodeDouble(value.value.toDouble())
    }

    override fun deserialize(decoder: Decoder): NumberWrapper {
        return NumberWrapper(decoder.decodeDouble())
    }
}