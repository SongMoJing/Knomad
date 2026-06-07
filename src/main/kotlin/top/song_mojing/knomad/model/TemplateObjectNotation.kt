package top.song_mojing.knomad.model

import kotlinx.serialization.Serializable
import top.song_mojing.knomad.model.serializer_ton.*

/**
 * 模板对象
 */
@Serializable(with = TonItemSerializer::class)
sealed class TonItem

@Serializable(with = TonObjectSerializer::class)
class TonObject(val fields: Map<String, TonItem>): TonItem()

@Serializable(with = TonArraySerializer::class)
class TonArray(val items: List<TonItem>): TonItem()

/**
 * 模板对象值
 */
@Serializable
sealed class TonValue: TonItem()

@Serializable
class TonString(val value: TemplateString) : TonValue()

@Serializable(with = TonNumberSerializer::class)
class TonNumber(val value: Number): TonValue()

@Serializable
class TonBoolean(val value: Boolean): TonValue()

@Serializable
class TonNull: TonValue()

