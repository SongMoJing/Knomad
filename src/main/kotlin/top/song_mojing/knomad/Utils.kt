package top.song_mojing.knomad

import top.song_mojing.knomad.model.Placeholder
import top.song_mojing.knomad.model.StringTemplate
import top.song_mojing.knomad.model.StringValue
import top.song_mojing.knomad.model.TonArray
import top.song_mojing.knomad.model.TonBoolean
import top.song_mojing.knomad.model.TonItem
import top.song_mojing.knomad.model.TonNull
import top.song_mojing.knomad.model.TonNumber
import top.song_mojing.knomad.model.TonObject
import top.song_mojing.knomad.model.TonString
import top.song_mojing.knomad.model.TonTemplate
import top.song_mojing.knomad.model.serializer.NumberWrapper
import top.song_mojing.knomad.model.toTemplate
import top.song_mojing.knomad.model.toTonString
import kotlin.collections.map
import kotlin.collections.mapValues


fun TonItem.parse(context: Context): TonItem {
    return when (this) {
        is TonObject -> TonObject(this.mapValues { it.value.parse(context) })

        is TonArray -> TonArray(this.map { it.parse(context) })

        is TonTemplate -> {
            val struct = this.value
            when (struct.key) {
                "variables" -> {
                    context.getVariableValue(struct.value)?.parse(context) ?: TonNull()
                }
                "env" -> {
                    System.getenv(struct.value)?.let { TonString(it.toTemplate) } ?: TonNull()
                }
                else -> TonNull()
            }
        }

        is TonString -> {
            val items = this.value.value
            if (items.size == 1 && items[0] is Placeholder) {
                val placeholder = items[0] as Placeholder
                if (placeholder.key == "variables") {
                    context.getVariableValue(placeholder.value)?.let { return it.parse(context) }
                }
            }

            val resolvedString = items.joinToString("") { item ->
                when (item) {
                    is StringValue -> item.value
                    is Placeholder -> {
                        when (item.key) {
                            "variables" -> {
                                when (val varItem = context.getVariableValue(item.value)?.parse(context)) {
                                    is TonString -> varItem.value.parse(context)
                                    is TonNumber -> varItem.value.value.toString()
                                    is TonBoolean -> varItem.value.toString()
                                    null -> "null"
                                    else -> varItem.toString()
                                }
                            }
                            "env" -> System.getenv(item.value) ?: "null"
                            else -> "null"
                        }
                    }
                }
            }
            TonString(resolvedString.toTemplate)
        }

        else -> this
    }
}

fun StringTemplate.parse(context: Context): String {
    return this.value.joinToString("") { item ->
        when (item) {
            is StringValue -> item.value
            is Placeholder -> {
                when (item.key) {
                    "env" -> System.getenv(item.value) ?: ""
                    "variables" -> {
                        when (val varItem = context.getVariableValue(item.value)?.parse(context)) {
                            is TonString -> {
                                varItem.value.parse(context)
                            }
                            is TonNumber -> varItem.value.value.toString()
                            is TonBoolean -> varItem.value.toString()
                            null -> ""
                            else -> varItem.toString()
                        }
                    }
                    else -> ""
                }
            }
        }
    }
}

fun Any?.toTonItem(): TonItem {
    return when (this) {
        null -> TonNull()
        is String -> this.toTonString()
        is Number -> TonNumber(NumberWrapper(this))
        is Boolean -> TonBoolean(this)
        is Map<*, *> -> {
            val convertedMap = this.entries.associate { it.key.toString() to it.value.toTonItem() }
            TonObject(convertedMap)
        }
        is List<*> -> {
            TonArray(this.map { it.toTonItem() })
        }
        else -> this.toString().toTonString()
    }
}
