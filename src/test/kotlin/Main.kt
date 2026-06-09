import com.jayway.jsonpath.JsonPath
import io.ktor.client.*
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import top.song_mojing.knomad.model.HttpMethod.*
import top.song_mojing.knomad.model.HttpStatus
import top.song_mojing.knomad.model.TonBoolean
import top.song_mojing.knomad.model.TonNumber
import top.song_mojing.knomad.model.serializer.NumberWrapper
import top.song_mojing.knomad.model.toTonString
import top.song_mojing.knomad.model.tonArrayOf
import top.song_mojing.knomad.model.tonObjectOf
import top.song_mojing.knomad.Context
import top.song_mojing.knomad.Variable
import top.song_mojing.knomad.parse
import top.song_mojing.knomad.toTonItem
import kotlin.test.Test

class MainTest {
    @Test
    fun customOpenaiApi(): Unit = runBlocking {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000
            }
        }
        val config = loadConfig("openai-api.custom.yaml")
        val context = Context(
            variableMapper = mapOf(
                "Model" to "Qwen2.5-7B-Instruct".toTonString(),
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
                "Count" to TonNumber(NumberWrapper(1)),
                "TRUE" to TonBoolean(true),
            ),
            variables = config.variables
        )
        config.endpoints.forEach { (endpointName, endpoint) ->
            endpoint.methods.forEach { (method, struct) ->
                val block: HttpRequestBuilder.() -> Unit = {
                    struct.request.headers?.forEach { (key, value) ->
                        println("请求头: $key: ${value.parse(context)}")
                        header(key, value.parse(context))
                    }
                    struct.request.body?.parse(context)?.let { body ->
                        println("请求体: ${Json.encodeToString(body)}")
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
                        println(" [URL] $url")
                        val responseBody = res.bodyAsText()
                        println(" [Response] $responseBody")
                        if (res.status.value in 200..299) {
                            val successResponseConfig = struct.response.find {
                                when (it.httpCode) {
                                    is HttpStatus.Status ->
                                        it.httpCode == HttpStatus.Status.Success
                                    is HttpStatus.Code ->
                                        it.httpCode.code == res.status.value
                                }
                            }
                            successResponseConfig?.values?.forEach { valueConfig ->
                                try {
                                    val extractedObject: Any? = JsonPath.read(responseBody, valueConfig.path)
                                    println(" [JSON Path] 变量 '${valueConfig.name}', 值为: $extractedObject")
                                } catch (e: Exception) {
                                    println(" [JSON Path] 解析路径 '${valueConfig.path}' 出错: ${e.message}")
                                }
                            }
                        } else {
                            val errorResponseConfig = struct.response.find {
                                when (it.httpCode) {
                                    is HttpStatus.Status ->
                                        it.httpCode == HttpStatus.Status.Failure
                                    is HttpStatus.Code ->
                                        it.httpCode.code == res.status.value
                                }
                            }
                            errorResponseConfig?.values?.forEach { valueConfig ->
                                try {
                                    val extractedObject: Any? = JsonPath.read(responseBody, valueConfig.path)
                                    println(" [异常状态码提取] '${valueConfig.name}': $extractedObject")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
            }
        }
    }

    @Test
    fun httpbinTest(): Unit = runBlocking {
        val client = HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(WebSockets) {
                pingIntervalMillis = 20_000
            }
        }
        val config = loadConfig("httpbin.yaml")
        val context = Context(
            variableMapper = mapOf(
                "Authorization" to "TestAPI".toTonString(),
                "Content" to tonArrayOf(tonObjectOf("data" to "hello"))
            ),
            variables = config.variables
        )
        config.endpoints.forEach { (endpointName, endpoint) ->
            endpoint.methods.forEach { (method, struct) ->
                val block: HttpRequestBuilder.() -> Unit = {
                    struct.request.headers?.forEach { (key, value) ->
                        println("请求头: $key: ${value.parse(context)}")
                        header(key, value.parse(context))
                    }
                    struct.request.body?.parse(context)?.let { body ->
                        println("请求体: ${Json.encodeToString(body)}")
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
                        println(" [URL] $url")
                        val responseBody = res.bodyAsText()
                        println(" [Response] $responseBody")
                        if (res.status.value in 200..299) {
                            val successResponseConfig = struct.response.find {
                                when (it.httpCode) {
                                    is HttpStatus.Status ->
                                        it.httpCode == HttpStatus.Status.Success
                                    is HttpStatus.Code ->
                                        it.httpCode.code == res.status.value
                                }
                            }
                            successResponseConfig?.values?.forEach { valueConfig ->
                                try {
                                    val extractedObject: Any? = JsonPath.read(responseBody, valueConfig.path)
                                    println(" [JSON Path] 变量 '${valueConfig.name}', 值为: $extractedObject")
                                } catch (e: Exception) {
                                    println(" [JSON Path] 解析路径 '${valueConfig.path}' 出错: ${e.message}")
                                }
                            }
                        } else {
                            val errorResponseConfig = struct.response.find {
                                when (it.httpCode) {
                                    is HttpStatus.Status ->
                                        it.httpCode == HttpStatus.Status.Failure
                                    is HttpStatus.Code ->
                                        it.httpCode.code == res.status.value
                                }
                            }
                            errorResponseConfig?.values?.forEach { valueConfig ->
                                try {
                                    val extractedObject: Any? = JsonPath.read(responseBody, valueConfig.path)
                                    println(" [异常状态码提取] '${valueConfig.name}': $extractedObject")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
            }
        }
    }
}
