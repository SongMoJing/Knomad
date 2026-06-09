package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import top.song_mojing.knomad.model.*

object TonItemSerializer : KSerializer<TonItem> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TonItem")

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: TonItem) {
        when (value) {
            is TonObject -> TonObjectSerializer.serialize(encoder, value)
            is TonArray -> TonArraySerializer.serialize(encoder, value)
            is TonString -> TemplateSerializer.serialize(encoder, value.value)
            is TonNumber -> NumberSerializer.serialize(encoder, value.value)
            is TonBoolean -> encoder.encodeBoolean(value.value)
            is TonNull -> encoder.encodeNull()
            is TonTemplate -> TemplateSerializer.serialize(encoder, value.value)
        }
    }

    override fun deserialize(decoder: Decoder): TonItem {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("TonItemSerializer 仅支持 JSON 反序列化，当前 decoder: ${decoder::class.simpleName}")
        val element: JsonElement = jsonDecoder.decodeJsonElement()
        return convertJsonToTonItem(element)
    }

    internal fun convertJsonToTonItem(element: JsonElement): TonItem {
        return when (element) {
            is JsonNull -> TonNull()
            is JsonObject -> {
                val map = mutableMapOf<String, TonItem>()
                for ((k, v) in element) {
                    map[k] = convertJsonToTonItem(v)
                }
                TonObject(map)
            }
            is JsonPrimitive -> {
                if (element.isString) {
                    val content = element.content
                    if (content.contains("{{")) {
                        when (val value = TemplateSerializer.parseTemplateString(content)) {
                            is StringTemplate -> TonString(value)
                            is Struct -> TonTemplate(value)
                        }
                    } else {
                        content.toBooleanStrictOrNull()?.let { TonBoolean(it) }
                            ?: content.toLongOrNull()?.let { TonNumber(NumberWrapper(it)) }
                            ?: content.toDoubleOrNull()?.let { TonNumber(NumberWrapper(it)) }
                            ?: when (val value = TemplateSerializer.parseTemplateString(content)) {
                                is StringTemplate -> TonString(value)
                                is Struct -> TonTemplate(value)
                            }
                    }
                } else {
                    val content = element.content
                    content.toBooleanStrictOrNull()?.let { return TonBoolean(it) }
                    content.toLongOrNull()?.let { return TonNumber(NumberWrapper(it)) }
                    content.toDoubleOrNull()?.let { return TonNumber(NumberWrapper(it)) }
                    TonString(StringTemplate.of(content))
                }
            }
            else -> {
                try {
                    val arr = element.jsonArray
                    TonArray(arr.map { convertJsonToTonItem(it) })
                } catch (_: Exception) {
                    TonString(StringTemplate.of(element.toString()))
                }
            }
        }
    }
}