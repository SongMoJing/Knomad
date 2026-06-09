package top.song_mojing.knomad.model.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.HttpStatus
import top.song_mojing.knomad.model.KnomadType

object HttpStatusSerializer : KSerializer<HttpStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HttpStatus", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): HttpStatus {
        val contentStr = decoder.decodeString().trim()
        val split = contentStr.split(":")
        return when (split[0]) {
            "code" -> HttpStatus.Code(split[1].toInt())
            "type" -> when (split[1].lowercase()) {
                "success" -> HttpStatus.Status.Success
                "failed" -> HttpStatus.Status.Failure
                else -> throw SerializationException("Invalid HttpStatus type")
            }

            else -> throw SerializationException("Invalid HttpStatus")
        }
    }

    override fun serialize(encoder: Encoder, value: HttpStatus) {
        when (value) {
            is HttpStatus.Code -> encoder.encodeString("code:${value.code}")
            is HttpStatus.Status -> encoder.encodeString(
                "type:${
                    when (value) {
                        HttpStatus.Status.Success -> "success"
                        HttpStatus.Status.Failure -> "failed"
                    }
                }"
            )
        }
    }
}