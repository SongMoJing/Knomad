package top.song_mojing.knomad.model.serializer_ton

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.*
import net.mamoe.yamlkt.*

object TonItemSerializer : KSerializer<TonItem> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("TonItem")

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: TonItem) {
        when (value) {
            is TonObject -> TonObjectSerializer.serialize(encoder, value)
            is TonArray -> TonArraySerializer.serialize(encoder, value)
            is TonString -> TemplateStringSerializer.serialize(encoder, value.value)
            is TonNumber -> TonNumberSerializer.serialize(encoder, value)
            is TonBoolean -> encoder.encodeBoolean(value.value)
            is TonNull -> encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): TonItem {
        val element = decoder.decodeSerializableValue(YamlElement.serializer())
        return convertToTonItem(element)
    }

    private fun convertToTonItem(element: YamlElement): TonItem {
        return when (element) {
            is YamlMap -> {
                val map = mutableMapOf<String, TonItem>()
                for ((k, v) in element) {
                    val keyStr = (k as? YamlPrimitive)?.content ?: k.toString()
                    map[keyStr] = convertToTonItem(v)
                }
                TonObject(map)
            }
            is YamlList -> {
                TonArray(element.map { convertToTonItem(it) })
            }
            is YamlNull -> TonNull()
            is YamlPrimitive -> {
                val content = element.content ?: ""
                if (content.contains("{{")) {
                    TonString(TemplateStringSerializer.parseTemplateString(content))
                } else {
                    content.toBooleanStrictOrNull()?.let { TonBoolean(it) }
                        ?: content.toLongOrNull()?.let { TonNumber(it) }
                        ?: content.toDoubleOrNull()?.let { TonNumber(it) }
                        ?: TonString(TemplateStringSerializer.parseTemplateString(content))
                }
            }
        }
    }
}