import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import top.song_mojing.knomad.model.HttpMethod.*
import top.song_mojing.knomad.model.tonArrayOf
import top.song_mojing.knomad.model.tonObjectOf
import top.song_mojing.knomad.validator.Context
import top.song_mojing.knomad.validator.parse
import kotlin.test.Test

class MainTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = HttpClient()
        val config = loadConfig("OpenAI.yaml")
        val context = Context(
            variableMapper = mapOf(
                "Model" to "Qwen2.5-7B-Instruct",
                "Content" to tonArrayOf(
                    tonObjectOf(
                        "role" to "system",
                        "content" to "你是一个有用的助手"
                    ),
                    tonObjectOf(
                        "role" to "user",
                        "content" to "这是一个测试，请回复：你好"
                    )
                ),
            ),
            variables = config.variables,
            typeDefinition = config.types
        )
        config.endpoints.forEach { (endpointName, endpoint) ->
            val block: HttpRequestBuilder.() -> Unit = {
                endpoint.request.headers?.forEach { (key, value) ->
                    header(key, value.parse(context))
                }
                setBody(
                    Json.encodeToString(endpoint.request.body?.parse(context))
                )
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
                    endpoint.request.params?.forEach { (key, value) ->
                        builder.addQueryParameter(key, value.parse(context))
                    }
                    return@let builder
                }
                ?.build()
                ?.toString()
                ?.let { url ->
                    val res = when (endpoint.request.method) {
                        GET -> client.get(url, block)
                        POST -> client.post(url, block)
                        PUT -> client.put(url, block)
                        DELETE -> client.delete(url, block)
                        PATCH -> client.patch(url, block)
                        HEAD -> client.head(url, block)
                        OPTIONS -> client.options(url, block)
                        else -> return@forEach
                    }
                    println("++++++++++++++++[${endpoint.request.method}] $endpointName ================================")
                    println("URL: $url")
                    println(res.bodyAsText())
                }
        }
    }
}
