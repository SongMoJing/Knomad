package top.song_mojing.knomad.model.serialize.serializer_ton

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.TonObject

object TonObjectSerializer : KSerializer<TonObject> {
    private val mapSerializer = MapSerializer(String.serializer(), TonItemSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): TonObject {
        return TonObject(decoder.decodeSerializableValue(mapSerializer))
    }

    override fun serialize(encoder: Encoder, value: TonObject) {
        encoder.encodeSerializableValue(mapSerializer, value.fields)
    }
}