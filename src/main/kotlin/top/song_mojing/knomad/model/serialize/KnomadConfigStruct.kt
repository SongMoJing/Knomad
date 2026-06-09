package top.song_mojing.knomad.model.serialize

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.KnomadType
import top.song_mojing.knomad.model.HttpMethod
import top.song_mojing.knomad.model.HttpStatus
import top.song_mojing.knomad.model.MimeType
import top.song_mojing.knomad.model.StringTemplate
import top.song_mojing.knomad.model.Template
import top.song_mojing.knomad.model.TonObject
import top.song_mojing.knomad.model.serializer.TonObjectSerializer

@Serializable
data class KnomadConfigStruct(
    val version: String,
    val baseUrl: String,
    val variables: Map<String, VariableStruct> = emptyMap(),
    val endpoints: Map<String, EndpointStruct> = emptyMap()
)

@Serializable
data class VariableStruct(
    val type: KnomadType,
    val required: Boolean,
    val description: String? = null
)

@Serializable
data class EndpointStruct(
    val path: String,
    val methods: Map<HttpMethod, EndpointOperationStruct>
)

@Serializable
data class EndpointOperationStruct(
    val request: RequestConfigStruct,
    val response: List<ResponseConfigStruct> = emptyList()
)

@Serializable
data class RequestConfigStruct(
    val query: Map<String, StringTemplate>? = null,
    val headers: Map<String, StringTemplate>? = null,
    val body: TonObject? = null
)

@Serializable
data class ResponseConfigStruct(
    val httpCode: HttpStatus,
    val type: MimeType,
    val values: List<ResponseValueStruct> = emptyList()
)

@Serializable
data class ResponseValueStruct(
    val name: String,
    val type: KnomadType,
    val path: String
)