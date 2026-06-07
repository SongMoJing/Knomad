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
    val variables: Map<String, VariableStruct> = emptyMap(),
    val types: Map<String, CustomTypeStruct> = emptyMap(),
    val baseUrl: String,
    val endpoints: Map<String, EndpointStruct> = emptyMap()
)

@Serializable
data class VariableStruct(
    val type: KnomadType,
    val required: Boolean,
    val description: String? = null
)

@Serializable
data class CustomTypeStruct(
    val description: String? = null,
    val struct: Map<String, TypeStructFieldStruct> = emptyMap()
)

@Serializable
data class TypeStructFieldStruct(
    val type: KnomadType,
    val description: String? = null,
    val struct: Map<String, TypeStructFieldStruct>? = null
)

@Serializable
data class EndpointStruct(
    val path: String,
    val request: RequestConfigStruct,
    val response: List<ResponseConfigStruct> = emptyList()
)

@Serializable
data class RequestConfigStruct(
    val method: HttpMethod,
    val params: Map<String, TemplateString>? = null,
    val headers: Map<String, TemplateString>? = null,
    @Serializable(with = TonObjectSerializer::class)
    val body: TonObject? = null
)

@Serializable
data class ResponseConfigStruct(
    val httpCode: String,
    val type: MimeType,
    val values: List<ResponseValueStruct> = emptyList()
)

@Serializable
data class ResponseValueStruct(
    val name: String,
    val type: KnomadType,
    val path: String
)