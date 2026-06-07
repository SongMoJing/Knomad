import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import top.song_mojing.knomad.model.HttpMethod.*
import top.song_mojing.knomad.model.KnomadValue
import top.song_mojing.knomad.model.new
import top.song_mojing.knomad.validator.Context
import top.song_mojing.knomad.validator.Variable
import top.song_mojing.knomad.validator.parse
import kotlin.test.Test

class MainTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = HttpClient()
        val config = loadConfig("OpenAI.yaml")
        val context = Context(
            mapOf(
                "Model" to "Qwen2.5-7B-Instruct",
                "Content" to "Hello World!"
            ),
            config.variables
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
            val res = when (endpoint.request.method) {
                GET -> client.get("${config.baseUrl}${endpoint.path}", block)
                POST -> client.post("${config.baseUrl}${endpoint.path}", block)
                PUT -> client.put("${config.baseUrl}${endpoint.path}", block)
                DELETE -> client.delete("${config.baseUrl}${endpoint.path}", block)
                PATCH -> client.patch("${config.baseUrl}${endpoint.path}", block)
                HEAD -> client.head("${config.baseUrl}${endpoint.path}", block)
                OPTIONS -> client.options("${config.baseUrl}${endpoint.path}", block)
                else -> return@forEach
            }
            println("++++++++++++++++[${endpoint.request.method}] ${endpointName}================================")
            println("URL: ${config.baseUrl}/${endpoint.path}")
            println(res.bodyAsText())
        }
    }
}
