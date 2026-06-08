package top.song_mojing.knomad.validator

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.serialize.CustomTypeStruct
import top.song_mojing.knomad.model.serialize.VariableStruct

class Context(
    variableMapper: Map<String, Any>,
    variables: Map<String, VariableStruct>,
    typeDefinition: Map<String, CustomTypeStruct>,
) {

    var variables: MutableMap<String, Variable> = mutableMapOf()

    init {
        this.variables = variables
            .mapValues { (key, value) ->
                val variable = Variable(
                    value.type.new(variableMapper[key]),
                    value.required,
                    value.description
                )
                if (variable.required) {
                    if (variable.value == KnomadValue.Undefined) {
                        throw RuntimeException("Variable $key is required, but not provided.")
                    }
                }
                return@mapValues variable
            }
            .toMutableMap()
    }

    fun getVariableValue(key: String): String? {
        return variables[key]?.value?.toString()
    }
}

data class Variable(
    val value: KnomadValue,
    val required: Boolean,
    val description: String? = null
)

fun TonItem.parse(context: Context): TonItem {
    return when (this) {
        is TonObject -> {
            TonObject(fields.mapValues { it.value.parse(context) })
        }

        is TonArray -> {
            TonArray(items.map { it.parse(context) })
        }

        is TonString -> {
            val resolvedString = this.value.parse(context)
            resolvedString.toTonString()
        }

        else -> this
    }
}

fun TemplateString.parse(context: Context): String {
    return when (this) {
        is TemplateString.StringTemplate -> {
            value.joinToString("") { item ->
                when (item) {
                    is TemplateString.StringTemplate.ValueItem.StringValue -> item.value
                    is TemplateString.StringTemplate.ValueItem.Placeholder -> {
                        when (item.key) {
                            "variables" -> context.getVariableValue(item.value)
                            "env" -> System.getenv(item.value)
                            else -> null
                        } ?: "null"
                    }
                }
            }
        }

        is TemplateString.Struct -> {
            when (this.key) {
                "variables" -> context.getVariableValue(this.value)
                "env" -> System.getenv(this.value)
                else -> null
            } ?: "null"
        }
    }
}
