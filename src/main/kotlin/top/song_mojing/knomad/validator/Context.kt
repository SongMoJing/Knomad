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

        // 新增：处理未带 $ 符号的严格占位符对象（TonTemplate）
        is TonTemplate -> {
            val struct = this.value
            when (struct.key) {
                "variables" -> {
                    // 获取对应的变量值，并递归对其进行 parse 解析，若不存在则返回 TonNull()
                    context.getVariableValue(struct.value)?.parse(context) ?: TonNull()
                }
                "env" -> {
                    // 获取系统环境变量，转为 TonString，若不存在则返回 TonNull()
                    System.getenv(struct.value)?.let { TonString(it.toTemplate) } ?: TonNull()
                }
                else -> TonNull()
            }
        }

        is TonString -> {
            val items = this.value.value

            // 特殊优化：如果整个字符串里面「只有一个占位符」且没有其他文本（形如 "${{variables.Content}}" ）
            // 我们直接把这个占位符代表的真实对象拿出来返回，保留它的原本类型（TonArray, TonNumber 等）
            if (items.size == 1 && items[0] is Placeholder) {
                val placeholder = items[0] as Placeholder
                if (placeholder.key == "variables") {
                    context.getVariableValue(placeholder.value)?.let { return it.parse(context) }
                }
            }

            // 否则，按照普通的文本片段拼接逻辑
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
                                    // 如果强行把复杂对象拼进字符串里，将其转为原生字符串展示
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