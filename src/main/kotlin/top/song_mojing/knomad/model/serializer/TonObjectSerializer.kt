package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import top.song_mojing.knomad.model.TonObject

object TonObjectSerializer : KSerializer<TonObject> {
    private val mapSerializer = MapSerializer(String.serializer(), TonItemSerializer)

    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): TonObject {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("TonObjectSerializer 仅支持 JSON 反序列化")
        val element = jsonDecoder.decodeJsonElement()
        if (element !is JsonObject) {
            throw SerializationException("TonObject 期望 JSON Object，实际: ${element::class.simpleName}")
        }
        val map = element.jsonObject.entries.associate { (k, v) ->
            k to TonItemSerializer.convertJsonToTonItem(v)
        }
        return TonObject(map)
    }

    override fun serialize(encoder: Encoder, value: TonObject) {
        encoder.encodeSerializableValue(mapSerializer, value)
    }
}