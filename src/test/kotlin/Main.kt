import net.mamoe.yamlkt.Yaml
import top.song_mojing.knomad.YamlValidator
import top.song_mojing.knomad.model.KnomadConfig
import java.io.File
import kotlin.test.Test

class MainTest {
    @Test
    fun test() {
        val schema = File(".note/test/schema.yaml").readText()
        val file = File(".note/test/test.yaml").readText()
        val error = YamlValidator.validate(
            schema,
            file
        )
        if (error.isEmpty()) {
            val config = Yaml.decodeFromString(KnomadConfig.serializer(), file)
            // 序列化
            val encode = Yaml.encodeToString(KnomadConfig.serializer(), config)
            println(encode)
        } else {
            error.forEach {
                println(it)
            }
        }
    }
}
