package top.song_mojing.knomad.validator

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.serialize.VariableStruct

class Context(
    variableMapper: Map<String, TonItem>,
    variables: Map<String, VariableStruct>
) {
    var variables: MutableMap<String, Variable> = mutableMapOf()

    init {
        this.variables = variables
            .mapValues { (key, value) ->
                val item = variableMapper[key]
                if (value.required && item == null) {
                    throw RuntimeException("Variable $key is required, but not provided.")
                }
                Variable(
                    value = item,
                    required = value.required,
                    description = value.description
                )
            }
            .toMutableMap()
    }

    fun getVariableValue(key: String): TonItem? {
        return variables[key]?.value
    }
}

data class Variable(
    val value: TonItem?,
    val required: Boolean,
    val description: String? = null
)

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
                                    is TonString -> varItem.value.unwrap()
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