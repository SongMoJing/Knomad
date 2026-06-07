import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.model.KnomadConfigStruct
import top.song_mojing.knomad.parser.yaml.YamlParser
import top.song_mojing.knomad.parser.yaml.YamlParserException
import kotlin.test.Test
import kotlin.text.Charsets.UTF_8

class MainTest {
    @Test
    fun test() {
        MainTest::class.java.getResource("test.yaml")?.let { uri ->
            val file = uri.readText(UTF_8)
            try {
                val config = YamlParser.parser(file)
                val encode = Yaml.encodeToString(KnomadConfigStruct.serializer(), config)
                println(encode)
            } catch (error: YamlParserException) {
                error.validatorExceptions.forEach {
                    println(it)
                }
            }
        }
    }
}
