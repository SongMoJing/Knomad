package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import net.mamoe.yamlkt.YamlElement
import top.song_mojing.knomad.model.serializer_ton.TonObjectSerializer

@Serializable(with = BaseTypeSerializer::class)
sealed class BaseType {
    @Serializable
    class String : BaseType()

    @Serializable
    class Bool : BaseType()

    @Serializable
    class Int : BaseType()

    @Serializable
    class Float : BaseType()

    @Serializable
    class List(val t1: BaseType) : BaseType() {
        override fun toString(): kotlin.String {
            return "List($t1)"
        }
    }

    @Serializable
    class Object(val t1: BaseType, val t2: BaseType) : BaseType() {
        override fun toString(): kotlin.String {
            return "Object($t1, $t2)"
        }
    }

    @Serializable
    class Other(val key: kotlin.String) : BaseType() {
        override fun toString(): kotlin.String {
            return key
        }
    }
}

@Serializable(with = TemplateStringSerializer::class)
sealed class TemplateString {
    @Serializable
    class StringTemplate(val value: List<ValueItem>) : TemplateString() {
        @Serializable
        sealed class ValueItem {
            class StringValue(val value: String) : ValueItem()
            class Placeholder(val key: String, val value: String) : ValueItem()
        }
    }

    @Serializable
    class Struct(val struct: BaseType.Other) : TemplateString()
}

@Suppress("unused")
@Serializable
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,
}

@Serializable(with = MimeTypeSerializer::class)
data class MimeType(
    val type: String,
    val subtype: String
)

@Serializable
data class KnomadConfigStruct(
    val variables: Map<String, Variable> = emptyMap(),
    val types: Map<String, CustomType> = emptyMap(),
    val baseUrl: String,
    val endpoints: Map<String, Endpoint> = emptyMap()
)

@Serializable
data class Variable(
    val type: BaseType,
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
    val type: BaseType,
    val description: String? = null,
    val struct: Map<String, StructField>? = null
)

@Serializable
data class Endpoint(
    val path: TemplateString,
    val request: RequestConfig,
    val response: List<ResponseConfig> = emptyList()
)

@Serializable
data class RequestConfig(
    val method: HttpMethod,
    val params: Map<String, TemplateString>? = null,
    val headers: Map<String, TemplateString>? = null,
    @Serializable(with = TonObjectSerializer::class)
    val body: TonObject? = null
)

@Serializable
data class ResponseConfig(
    val httpCode: String,
    val type: MimeType,
    val values: List<ResponseValue> = emptyList()
)

@Serializable
data class ResponseValue(
    val name: String,
    val type: BaseType,
    val path: String
)