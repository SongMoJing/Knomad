import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import top.song_mojing.knomad.model.HttpMethod.*
import top.song_mojing.knomad.model.TonItem
import top.song_mojing.knomad.model.TonNumber
import top.song_mojing.knomad.model.TonString
import top.song_mojing.knomad.model.knomadListOf
import top.song_mojing.knomad.model.knomadMapOf
import top.song_mojing.knomad.model.toKnomadValue
import top.song_mojing.knomad.model.toTonString
import top.song_mojing.knomad.model.tonArrayOf
import top.song_mojing.knomad.model.tonObjectOf
import top.song_mojing.knomad.validator.Context
import top.song_mojing.knomad.validator.parse
import kotlin.test.Test

class MainTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = HttpClient()
        val config = loadConfig("httpbin-test.yaml")
        val context = Context(
            variableMapper = mapOf(
                "Model" to "Qwen2.5-7B-Instruct".toKnomadValue(),
                "Content" to knomadListOf(
                    knomadMapOf(
                        "role" to "system",
                        "content" to "你是一个有用的助手"
                    ),
                    knomadMapOf(
                        "role" to "user",
                        "content" to "这是一个测试，请回复：你好"
                    )
                ),
                "Count" to 1.toKnomadValue(),
                "TRUE" to true.toKnomadValue(),
            ),
            variables = config.variables
        )
        config.endpoints.forEach { (endpointName, endpoint) ->
            endpoint.methods.forEach { (method, struct) ->
                val block: HttpRequestBuilder.() -> Unit = {
                    struct.request.headers?.forEach { (key, value) ->
                        header(key, value.parse(context))
                    }
                    struct.request.body?.parse(context)?.let { body ->
                        println("实际对象类型: ${body::class.qualifiedName}")
                        setBody(
                            Json.encodeToString(body)
                        )
                    }
                }
                config.baseUrl.toHttpUrlOrNull()
                    ?.newBuilder()
                    ?.let { builder ->
                        endpoint.path.split("/")
                            .filter { it.isNotEmpty() }
                            .forEach { builder.addPathSegment(it) }
                        return@let builder
                    }
                    ?.let { builder ->
                        struct.request.query?.forEach { (key, value) ->
                            builder.addQueryParameter(key, value.parse(context))
                        }
                        return@let builder
                    }
                    ?.build()
                    ?.toString()
                    ?.let { url ->
                        val res = when (method) {
                            GET -> client.get(url, block)
                            POST -> client.post(url, block)
                            PUT -> client.put(url, block)
                            DELETE -> client.delete(url, block)
                            PATCH -> client.patch(url, block)
                            HEAD -> client.head(url, block)
                            OPTIONS -> client.options(url, block)
                            else -> return@forEach
                        }
                        println("++++++++++++++++[${method}] $endpointName ================================")
                        println("URL: $url")
                        println(res.bodyAsText())
                    }
            }
        }
    }
}
