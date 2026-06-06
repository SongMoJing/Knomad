import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.parser.yaml.YamlValidator
import top.song_mojing.knomad.model.KnomadConfig
import top.song_mojing.knomad.parser.yaml.YamlParser
import top.song_mojing.knomad.parser.yaml.YamlParserException
import java.io.File
import kotlin.test.Test

class MainTest {
    @Test
    fun test() {
        val file = File(".note/test/test.yaml")
        try {
            val config = YamlParser.parser(file)
            val encode = Yaml.encodeToString(KnomadConfig.serializer(), config)
            println(encode)
        } catch (error: YamlParserException) {
            error.validatorExceptions.forEach {
                println(it)
            }
        }
    }
}
