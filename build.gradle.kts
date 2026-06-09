import org.gradle.plugins.signing.SigningExtension


plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "top.song-mojing"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // 日志
    implementation("org.slf4j:slf4j-nop:2.0.16")
    // YAML 解析
    implementation("net.mamoe.yamlkt:yamlkt:0.13.0")
    // JSON Schema
    implementation("com.networknt:json-schema-validator:1.0.87")
    // Kotlin 序列化
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    // Kotlin 协程核心库
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // Ktor HTTP 客户端
    testImplementation("io.ktor:ktor-client-core:3.0.1")
    testImplementation("io.ktor:ktor-client-okhttp:3.0.1")
    testImplementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
    testImplementation("io.ktor:ktor-client-contentnegotiation:3.0.1")
    // JsonPath
    testImplementation("com.jayway.jsonpath:json-path:2.9.0")
    // Json 解析
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates(group.toString(), "knomad", version.toString())
    pom {
        name.set("knomad")
        description.set("A lightweight Kotlin library.")
        url.set("https://github.com/SongMoJing/Knomad")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        // 开发者信息
        developers {
            developer {
                id.set("SongMoJing")
                name.set("SongMoJing")
                email.set("Song_Mojing@outlook.com")
            }
        }
        // 源码仓库信息
        scm {
            connection.set("scm:git:git://github.com/SongMoJing/Knomad.git")
            developerConnection.set("scm:git:ssh://github.com/SongMoJing/Knomad.git")
            url.set("https://github.com/SongMoJing/Knomad")
        }
    }
    // 自动发布到新版 Maven Central (Sonatype Central Portal)
    publishToMavenCentral()
    // 强制自动启用签名
    signAllPublications()
}

extensions.configure<SigningExtension>("signing") {
    useGpgCmd()
}