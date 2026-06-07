import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking
import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.model.serialize.KnomadConfigStruct
import top.song_mojing.knomad.parser.yaml.YamlParser
import top.song_mojing.knomad.parser.yaml.YamlParserException
import kotlin.test.Test
import kotlin.text.Charsets.UTF_8

class MainTest {
    @Test
    fun test(): Unit = runBlocking {
        val client = HttpClient()
        MainTest::class.java.getResource("test.yaml")?.let { uri ->
            val file = uri.readText(UTF_8)
            try {
                val config = YamlParser.parser(file)
                val response = client.post("https://httpbin.org/post")
                println(response)
            } catch (error: YamlParserException) {
                error.validatorExceptions.forEach {
                    println(it)
                }
            }
        }
    }
}
