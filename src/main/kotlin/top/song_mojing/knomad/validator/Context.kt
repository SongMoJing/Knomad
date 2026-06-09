package top.song_mojing.knomad.validator

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.Template.*
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
                if (value.required) {
                    if (item == null) {
                        throw RuntimeException("Variable $key is required, but not provided.")
                    }
                }
                val variable = Variable(
                    value = item,
                    required = value.required,
                    description = value.description
                )
                return@mapValues variable
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

        is TonString -> {
           val a = this.value.value.map {
                return@map when (it) {
                    is StringValue -> TonString(StringTemplate(listOf(StringValue(it.value))))
                    is Placeholder -> {
                        when (it.key) {
                            "variables" -> context.getVariableValue(it.value) as? TonString
                            "env" -> TonString(StringTemplate(listOf(StringValue(System.getenv(it.value)))))
                            else -> null
                        } ?: TonString(StringTemplate(listOf(StringValue("null"))))
                    }
                }
            }
        }

        is TonTemplate -> this

        else -> this
    } as TonItem
}
