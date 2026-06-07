package top.song_mojing.knomad.model.serialize

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.KnomadType
import top.song_mojing.knomad.model.HttpMethod
import top.song_mojing.knomad.model.MimeType
import top.song_mojing.knomad.model.TemplateString
import top.song_mojing.knomad.model.TonObject
import top.song_mojing.knomad.model.serialize.serializer_ton.TonObjectSerializer

@Serializable
data class KnomadConfigStruct(
    val variables: Map<String, Variable> = emptyMap(),
    val types: Map<String, CustomType> = emptyMap(),
    val baseUrl: String,
    val endpoints: Map<String, Endpoint> = emptyMap()
)

@Serializable
data class Variable(
    val type: KnomadType,
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
    val type: KnomadType,
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
    val type: KnomadType,
    val path: String
)