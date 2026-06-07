package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serialize.serializer_ton.TonArraySerializer
import top.song_mojing.knomad.model.serialize.serializer_ton.TonItemSerializer
import top.song_mojing.knomad.model.serialize.serializer_ton.TonNumberSerializer
import top.song_mojing.knomad.model.serialize.serializer_ton.TonObjectSerializer

/**
 * 模板对象
 */
@Serializable(with = TonItemSerializer::class)
sealed class TonItem

@Serializable(with = TonObjectSerializer::class)
class TonObject(var fields: Map<String, TonItem>): TonItem()

@JvmName("tonObjectOfItems")
fun tonObjectOf(vararg fields: Pair<String, TonItem>): TonObject {
    return TonObject(fields.toMap())
}

@JvmName("tonObjectOfStrings")
fun tonObjectOf(vararg fields: Pair<String, String>): TonObject {
    return TonObject(
        fields.associate { (key, value) ->
            key to value.toTonString()
        }
    )
}

@Serializable(with = TonArraySerializer::class)
class TonArray(var items: List<TonItem>): TonItem()

fun tonArrayOf(vararg items: TonItem): TonArray {
    return TonArray(items.toList())
}

/**
 * 模板对象值
 */
@Serializable
sealed class TonValue: TonItem()

@Serializable
class TonString(var value: TemplateString) : TonValue()

@JvmName("tonStringOfString")
fun String.toTonString(): TonString {
    return TonString(
        TemplateString.StringTemplate(
            listOf(
                TemplateString.StringTemplate.ValueItem.StringValue(
                    this
                )
            )
        )
    )
}

@Serializable(with = TonNumberSerializer::class)
class TonNumber(var value: Number): TonValue()

@Serializable
class TonBoolean(var value: Boolean): TonValue()

@Serializable
class TonNull: TonValue()

