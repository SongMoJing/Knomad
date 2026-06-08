package top.song_mojing.knomad.model

import top.song_mojing.knomad.model.KnomadValue.*
import top.song_mojing.knomad.validator.Context
import top.song_mojing.knomad.validator.parse

sealed class KnomadValue {
    class String(val value: kotlin.String) : KnomadValue() {
        override fun toString(): kotlin.String = value
    }

    class Boolean(val value: kotlin.Boolean) : KnomadValue() {
        override fun toString(): kotlin.String = value.toString()
    }

    class Integer(val value: Long) : KnomadValue() {
        override fun toString(): kotlin.String = value.toString()
    }

    class Float(val value: Double) : KnomadValue() {
        override fun toString(): kotlin.String = value.toString()
    }

    class List(
        val value: kotlin.collections.List<KnomadValue>
    ) : KnomadValue() {
        override fun toString(): kotlin.String {
            return "[${value.joinToString { "\"$it\"" }}]"
        }
    }

    class Map(
        val value: kotlin.collections.Map<KnomadValue, KnomadValue>,
    ) : KnomadValue() {
        override fun toString(): kotlin.String {
            return "{${value.map { "\"${it.key}\": \"${it.value}\"" }.joinToString()}}"
        }
    }

    class Other(val key: kotlin.String) : KnomadValue() {
        override fun toString(): kotlin.String {
            return key
        }
    }

    object Null : KnomadValue() {
        override fun toString(): kotlin.String = "null"
    }

    object Undefined : KnomadValue() {
        override fun toString(): kotlin.String = "undefined"
    }
}

fun TonItem.toKnomadValue(
    context: Context
): KnomadValue {
    return when (this) {
        is TonObject -> {
            Map(this.fields
                .mapValues { (_, value) -> value.toKnomadValue(context) }
                .mapKeys { (key, _) -> String(key) }
            )
        }

        is TonArray -> {
            List(this.items.map {
                it.toKnomadValue(context)
            })
        }

        is TonString -> {
            String(this.value.parse(context))
        }

        is TonBoolean -> Boolean(this.value)
        is TonNull -> Null
        is TonNumber -> when (this.value) {
            is Int, Long -> Integer(this.value as? Long ?: return Null)
            is Float, Double -> Float(this.value as? Double ?: return Null)
            else -> Null
        }
    }
}

fun KnomadType.new(value: Any?): KnomadValue {
    if (value == null) return Undefined
    return when (this) {
        is KnomadType.Int -> Integer(value as? Long ?: return Null)
        is KnomadType.String -> String(value as? String ?: return Null)
        is KnomadType.Bool -> Boolean(value as? Boolean ?: return Null)
        is KnomadType.Float -> Float(value as? Double ?: return Null)
        is KnomadType.List -> List((value as? List<*>)?.map {
            when (it) {
                is KnomadType -> it.new(it)
                else -> Other(it.toString())
            }
        } ?: return Null)

        is KnomadType.Map -> {
            Map((value as? Map<*, *>)?.mapKeys { (key, _) ->
                when (key) {
                    is KnomadType -> key.new(key)
                    else -> Other(key.toString())
                }
            }?.mapValues { (_, value) ->
                when (value) {
                    is KnomadType -> value.new(value)
                    else -> Other(value.toString())
                }
            }
                ?: return Null)
        }

        is KnomadType.Custom -> {
            Other(value.toString())
        }
    }
}