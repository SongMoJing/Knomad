package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serializer.HttpStatusSerializer
import top.song_mojing.knomad.model.serializer.KnomadTypeSerializer
import top.song_mojing.knomad.model.serializer.MimeTypeSerializer
import top.song_mojing.knomad.model.serializer.StringTemplateSerializer
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
sealed class Template

@Serializable(with = StringTemplateSerializer::class)
class StringTemplate(val value: List<ValueItem>) : Template() {

    companion object {
        fun of(text: String): StringTemplate = StringTemplate(listOf(StringValue(text)))
    }
}

val String.toTemplate: StringTemplate
    get() = StringTemplate(listOf(StringValue(this)))

@Serializable
sealed class ValueItem

class StringValue(val value: String) : ValueItem()
class Placeholder(val key: String, val value: String) : ValueItem()

@Serializable
class Struct(
    val key: String,
    val value: String
) : Template()

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

@Serializable(with = HttpStatusSerializer::class)
sealed class HttpStatus {
    sealed class Status: HttpStatus() {
        object Success : Status()
        object Failure : Status()
    }

    class Code(val code: Int) : HttpStatus()
}

@Serializable(with = MimeTypeSerializer::class)
data class MimeType(
    val type: String,
    val subtype: String
)
