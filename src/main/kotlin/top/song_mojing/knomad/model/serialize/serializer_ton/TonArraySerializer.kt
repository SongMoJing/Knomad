package top.song_mojing.knomad.model.serialize.serializer_ton

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.TonArray

object TonArraySerializer : KSerializer<TonArray> {

    private val listSerializer = ListSerializer(TonItemSerializer)

    override val descriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder) = TonArray(decoder.decodeSerializableValue(listSerializer))
    override fun serialize(encoder: Encoder, value: TonArray) = encoder.encodeSerializableValue(listSerializer, value.items)
}