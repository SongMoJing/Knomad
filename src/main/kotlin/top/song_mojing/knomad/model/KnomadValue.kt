package top.song_mojing.knomad.model

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

    object Null: KnomadValue() {
        override fun toString(): kotlin.String = "null"
    }

    object Undefined: KnomadValue() {
        override fun toString(): kotlin.String = "undefined"
    }
}

fun KnomadType.new(value: Any?): KnomadValue {
    if (value == null) return KnomadValue.Undefined
    return when (this) {
        is KnomadType.Int -> KnomadValue.Integer(value as? Long ?: return KnomadValue.Null)
        is KnomadType.String -> KnomadValue.String(value as? String ?: return KnomadValue.Null)
        is KnomadType.Bool -> KnomadValue.Boolean(value as? Boolean ?: return KnomadValue.Null)
        is KnomadType.Float -> KnomadValue.Float(value as? Double ?: return KnomadValue.Null)
        is KnomadType.List -> KnomadValue.List((value as? List<*>)?.map {
            when (it) {
                is KnomadType -> it.new(it)
                else -> KnomadValue.Other(it.toString())
            }
        } ?: return KnomadValue.Null)

        is KnomadType.Map -> {
            KnomadValue.Map((value as? Map<*, *>)?.mapKeys { (key, _) ->
                when (key) {
                    is KnomadType -> key.new(key)
                    else -> KnomadValue.Other(key.toString())
                }
            }?.mapValues { (_, value) ->
                when (value) {
                    is KnomadType -> value.new(value)
                    else -> KnomadValue.Other(value.toString())
                }
            }
                ?: return KnomadValue.Null)
        }
        is KnomadType.Custom -> KnomadValue.Other(value.toString())
    }
}