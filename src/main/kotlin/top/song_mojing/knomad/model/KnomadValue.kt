package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serializer.NumberSerializer
import java.util.function.IntFunction

@Serializable
sealed class KnomadValue {
    @Serializable
    class String(val value: kotlin.String) : KnomadValue() {
        override fun toString(): kotlin.String = value
    }
    @Serializable
    class Boolean(val value: kotlin.Boolean) : KnomadValue() {
        override fun toString(): kotlin.String = value.toString()
    }
    @Serializable
    class Number(@Serializable(with = NumberSerializer::class) val value: kotlin.Number) : KnomadValue()
    @Serializable
    class List(val value: kotlin.collections.List<KnomadValue>) : KnomadValue(), kotlin.collections.List<KnomadValue> by value {
        @Suppress("DEPRECATION")
        @Deprecated("该方法已废弃，不建议在 TonArray 中使用")
        override fun <T> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? = super.toArray(generator)
    }
    @Serializable
    class Map(val value: kotlin.collections.Map<KnomadValue, KnomadValue>) : KnomadValue(), kotlin.collections.Map<KnomadValue, KnomadValue> by value
    @Serializable
    object Null : KnomadValue() {
        override fun toString(): kotlin.String = "null"
    }
    @Serializable
    object Undefined : KnomadValue() {
        override fun toString(): kotlin.String = "undefined"
    }
}

fun Any.toKnomadValue(): KnomadValue {
    return when (this) {
        is String -> KnomadValue.String(this)
        is Boolean -> KnomadValue.Boolean(this)
        is Number -> KnomadValue.Number(this)
        is Array<*> -> KnomadValue.List(this.map { it?.toKnomadValue() ?: KnomadValue.Null })
        is List<*> -> KnomadValue.List(this.map { it?.toKnomadValue() ?: KnomadValue.Null })
        else -> KnomadValue.Null
    }
}

fun knomadListOf(vararg values: KnomadValue): KnomadValue.List = KnomadValue.List(values.toList())

@JvmName("knomadListOfKnomadValue")
fun knomadMapOf(vararg pairs: Pair<KnomadValue, KnomadValue>): KnomadValue.Map = KnomadValue.Map(pairs.toMap())
@JvmName("knomadListOfStringValue")
fun knomadMapOf(vararg pairs: Pair<String, KnomadValue>): KnomadValue.Map = KnomadValue.Map(pairs.toMap().mapKeys { KnomadValue.String(it.key) })
@JvmName("knomadListOfString")
fun knomadMapOf(vararg pairs: Pair<String, String>): KnomadValue.Map = KnomadValue.Map(pairs.toMap()
    .mapKeys { KnomadValue.String(it.key) }
    .mapValues { KnomadValue.String(it.value) }
)
