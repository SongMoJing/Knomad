import kotlinx.coroutines.runBlocking
import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.model.serialize.KnomadConfigStruct
import top.song_mojing.knomad.parser.yaml.YamlParser
import top.song_mojing.knomad.parser.yaml.YamlParserException
import kotlin.test.Test
import kotlin.text.Charsets.UTF_8

class FileTest {
    @Test
    fun test(): Unit = runBlocking {
        val config = loadConfig("openai-api.yaml")
        val encode = Yaml.encodeToString(KnomadConfigStruct.serializer(), config)
        println(encode)
    }
}

fun loadConfig(fileName: String): KnomadConfigStruct {
    try {
        MainTest::class.java.getResource(fileName)?.let { uri ->
            val file = uri.readText(UTF_8)
            return YamlParser.parser(file)
        }
        throw Exception("YamlFileNotFound")
    } catch (error: YamlParserException) {
        error.validatorExceptions.forEach {
            println(it)
        }
        throw Exception("YamlParserException")
    }
}