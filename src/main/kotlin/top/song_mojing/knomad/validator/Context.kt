package top.song_mojing.knomad.validator

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.serialize.VariableStruct

class Context(
    variableMapper: Map<String, KnomadValue>,
    variables: Map<String, VariableStruct>
) {

    var variables: MutableMap<String, Variable> = mutableMapOf()

    init {
        this.variables = variables
            .mapValues { (key, value) ->
                val variable = Variable(
                    value = variableMapper[key] ?: KnomadValue.Undefined,
                    required = value.required,
                    description = value.description
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
        is TonObject -> TonObject(this.mapValues { it.value.parse(context) })

        is TonArray -> TonArray(this.map { it.parse(context) })

        is TonString -> this.value.parse(context).toTonString()

        else -> this
    }
}

fun Template.parse(context: Context): String {
    return when (this) {
        is Template.StringTemplate -> {
            value.joinToString("") { item ->
                when (item) {
                    is Template.StringTemplate.ValueItem.StringValue -> item.value
                    is Template.StringTemplate.ValueItem.Placeholder -> {
                        when (item.key) {
                            "variables" -> context.getVariableValue(item.value)
                            "env" -> System.getenv(item.value)
                            else -> null
                        } ?: "null"
                    }
                }
            }
        }

        is Template.Struct -> {
            when (this.key) {
                "variables" -> context.getVariableValue(this.value)
                "env" -> System.getenv(this.value)
                else -> null
            } ?: "null"
        }
    }
}