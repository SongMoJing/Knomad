package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serializer.NumberWrapper
import top.song_mojing.knomad.model.serializer.TonArraySerializer
import top.song_mojing.knomad.model.serializer.TonItemSerializer
import top.song_mojing.knomad.model.serializer.TonObjectSerializer
import java.util.function.IntFunction

/**
 * 模板对象
 */
@Serializable(with = TonItemSerializer::class)
sealed class TonItem

@Serializable(with = TonObjectSerializer::class)
class TonObject(private val fields: Map<String, TonItem>) : TonItem(), Map<String, TonItem> by fields

@Serializable(with = TonArraySerializer::class)
class TonArray(private val items: List<TonItem>) : TonItem(), List<TonItem> by items {
    @Suppress("DEPRECATION")
    @Deprecated("该方法已废弃，不建议在 TonArray 中使用")
    override fun <T> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? = super.toArray(generator)
}

/**
 * 模板对象值
 */
@Serializable
sealed class TonValue : TonItem()

@Serializable
class TonString(var value: StringTemplate) : TonValue()

@Serializable
class TonTemplate(var value: Struct) : TonValue()

@Serializable
class TonNumber(var value: NumberWrapper) : TonValue()

@Serializable
class TonBoolean(var value: Boolean) : TonValue()

@Serializable
class TonNull : TonValue()

fun tonArrayOf(vararg items: TonItem): TonArray = TonArray(items.toList())

@JvmName("tonObjectOfItems")
fun tonObjectOf(vararg fields: Pair<String, TonItem>): TonObject = TonObject(fields.toMap())

@JvmName("tonObjectOfStrings")
fun tonObjectOf(vararg fields: Pair<String, String>): TonObject = TonObject(
    fields.associate { (key, value) ->
        key to value.toTonString()
    }
)

@JvmName("tonStringOfString")
fun String.toTonString(): TonString {
    return TonString(
        StringTemplate(
            listOf(
                StringValue(
                    this
                )
            )
        )
    )
}