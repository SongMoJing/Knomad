package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import top.song_mojing.knomad.model.TonArray

/**
 * TonArray 的自定义序列化器
 */
object TonArraySerializer : KSerializer<TonArray> {

    private val listSerializer = ListSerializer(TonItemSerializer)

    override val descriptor = listSerializer.descriptor

    override fun deserialize(decoder: Decoder): TonArray {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("TonArraySerializer 仅支持 JSON 反序列化")
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonArray) {
            throw SerializationException("TonArray 期望 JSON Array，实际: ${element::class.simpleName}")
        }
        val list = element.jsonArray.map { TonItemSerializer.convertJsonToTonItem(it) }
        return TonArray(list)
    }

    override fun serialize(encoder: Encoder, value: TonArray) {
        encoder.encodeSerializableValue(listSerializer, value)
    }
}