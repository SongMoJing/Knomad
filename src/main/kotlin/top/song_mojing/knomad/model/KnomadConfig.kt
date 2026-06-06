package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.YamlElement

@Serializable(with = TypeSerializer::class)
sealed class Type {
    @Serializable
    class String : Type()

    @Serializable
    class Bool : Type()

    @Serializable
    class Int : Type()

    @Serializable
    class Float : Type()

    @Serializable
    class List(val t1: Type) : Type() {
        override fun toString(): kotlin.String {
            return "List($t1)"
        }
    }

    @Serializable
    class Object(val t1: Type, val t2: Type) : Type() {
        override fun toString(): kotlin.String {
            return "Object($t1, $t2)"
        }
    }

    @Serializable
    class Other(val key: kotlin.String) : Type() {
        override fun toString(): kotlin.String {
            return key
        }
    }
}

@Serializable
enum class ResponseType {
    Json
}

@Serializable
data class KnomadConfig(
    val variables: Map<String, Variable> = emptyMap(),
    val types: Map<String, CustomType> = emptyMap(),
    val baseUrl: String,
    val endpoints: Map<String, Endpoint> = emptyMap()
)

@Serializable
data class Variable(
    val type: Type,
    val required: Boolean,
    val description: String? = null
)

@Serializable
data class CustomType(
    val description: String? = null,
    val struct: Map<String, StructField> = emptyMap()
)

@Serializable
data class StructField(
    val type: Type,
    val description: String? = null,
    val struct: Map<String, StructField>? = null
)

@Serializable
data class Endpoint(
    val path: String,
    val request: RequestConfig,
    val response: List<ResponseConfig> = emptyList()
)

@Serializable
data class RequestConfig(
    val method: String,
    val params: Map<String, YamlElement>? = null,
    val headers: Map<String, YamlElement>? = null,
    val body: Map<String, YamlElement>? = null
)

@Serializable
data class ResponseConfig(
    val httpCode: String,
    val type: ResponseType,
    val values: List<ResponseValue> = emptyList()
)

@Serializable
data class ResponseValue(
    val name: String,
    val type: Type,
    val path: String
)