package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serialize.BaseTypeSerializer
import top.song_mojing.knomad.model.serialize.MimeTypeSerializer
import top.song_mojing.knomad.model.serialize.TemplateStringSerializer


@Serializable(with = BaseTypeSerializer::class)
sealed class KnomadType {
    @Serializable
    class String(val value: kotlin.String) : KnomadType()

    @Serializable
    class Bool(val value: Boolean) : KnomadType()

    @Serializable
    class Int(val value: kotlin.Long) : KnomadType()

    @Serializable
    class Float(val value: kotlin.Double) : KnomadType()

    @Serializable
    class List(val t1: KnomadType) : KnomadType() {
        override fun toString(): kotlin.String {
            return "List($t1)"
        }
    }

    @Serializable
    class Object(val t1: KnomadType, val t2: KnomadType) : KnomadType() {
        override fun toString(): kotlin.String {
            return "Object($t1, $t2)"
        }
    }

    @Serializable
    class Other(val key: kotlin.String) : KnomadType() {
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
    class Struct(val struct: KnomadType.Other) : TemplateString()
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
