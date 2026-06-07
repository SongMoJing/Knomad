package top.song_mojing.knomad.model.serialize.serializer_ton

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.TonNumber

object TonNumberSerializer : KSerializer<TonNumber> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("TonNumber", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): TonNumber {
        val str = decoder.decodeString().trim()
        val number = str.toLongOrNull() ?: str.toDoubleOrNull() ?: 0.0
        return TonNumber(number)
    }

    override fun serialize(encoder: Encoder, value: TonNumber) {
        encoder.encodeString(value.value.toString())
    }
}