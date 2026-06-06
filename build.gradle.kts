plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
}

group = "top.song_mojing"
version = "0.1.0"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    // Ktor HTTP 客户端
    implementation("io.ktor:ktor-client-core:3.0.1")
    implementation("io.ktor:ktor-client-okhttp:3.0.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
    // JsonPath
    implementation("com.jayway.jsonpath:json-path:2.9.0")
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

kotlin {
    jvmToolchain(11)
}

tasks.test {
    useJUnitPlatform()
}