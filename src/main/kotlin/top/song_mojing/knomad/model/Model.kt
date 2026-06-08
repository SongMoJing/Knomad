package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serializer.KnomadTypeSerializer
import top.song_mojing.knomad.model.serializer.MimeTypeSerializer
import top.song_mojing.knomad.model.serializer.TemplateSerializer

@Serializable(with = KnomadTypeSerializer::class)
sealed class KnomadType {
    @Serializable
    class String : KnomadType()
    @Serializable
    class Boolean : KnomadType()
    @Serializable
    class Number : KnomadType()
    @Serializable
    class List : KnomadType()
    @Serializable
    class Map : KnomadType()
}

@Serializable(with = TemplateSerializer::class)
sealed class Template {
    @Serializable
    class StringTemplate(val value: List<ValueItem>) : Template() {
        @Serializable
        sealed class ValueItem {
            class StringValue(val value: String) : ValueItem()
            class Placeholder(val key: String, val value: String) : ValueItem()
        }
    }

    @Serializable
    class Struct(
        val key: String,
        val value: String
    ) : Template()
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
