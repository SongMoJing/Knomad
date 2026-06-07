package top.song_mojing.knomad.model.serialize

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import top.song_mojing.knomad.model.MimeType

object MimeTypeSerializer : KSerializer<MimeType> {

    override val descriptor = PrimitiveSerialDescriptor("MimeType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MimeType {
        val str = decoder.decodeString()
        val parts = str.split('/')
        return if (parts.size == 2) {
            MimeType(parts[0], parts[1])
        } else {
            MimeType("application", "octet-stream")
        }
    }

    override fun serialize(encoder: Encoder, value: MimeType) {
        encoder.encodeString("${value.type}/${value.subtype}")
    }
}