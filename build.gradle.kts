plugins {
    kotlin("jvm") version "1.9.24"
    application
    kotlin("plugin.serialization") version "1.9.24"
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.12"

dependencies {
    implementation(platform("io.ktor:ktor-bom:2.3.12"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")

    // 👇 این دو تا برای JSON
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")

    // (اختیاری؛ برای اطمینان IDE)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("ch.qos.logback:logback-classic:1.5.6")

    // 👇 احراز هویت و JWT
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("com.auth0:java-jwt:4.4.0")

    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("io.ktor:ktor-server-cors-jvm")

    // DB + Connection pool
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.53.0")

    // Driver (یکی را انتخاب کن)
    implementation("org.postgresql:postgresql:42.7.4")   // تولید
    // implementation("com.h2database:h2:2.3.232")       // توسعه/لوکال


    implementation("org.flywaydb:flyway-core:10.17.3")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.example.bambo.server.ApplicationKt")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(17)) }
}
kotlin { jvmToolchain(17) }
