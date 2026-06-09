package top.song_mojing.knomad

import top.song_mojing.knomad.model.*
import top.song_mojing.knomad.model.serialize.VariableStruct
import top.song_mojing.knomad.model.serializer.NumberWrapper

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
