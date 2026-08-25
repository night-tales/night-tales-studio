plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.ktor.plugin")
}

dependencies {
    implementation(project(":core"))
    implementation("io.ktor:ktor-server-core-jvm:3.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.3")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.3")
    implementation("org.jetbrains.exposed:exposed-core:0.58.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.58.0")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.flywaydb:flyway-core:11.8.2")
    implementation("org.flywaydb:flyway-database-postgresql:11.8.2")
    implementation("io.lettuce:lettuce-core:6.5.1.RELEASE")
    implementation("software.amazon.awssdk:s3:2.31.78")
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.3")
    testImplementation(kotlin("test"))
}

application { mainClass.set("com.hakayat.backend.ApplicationKt") }
kotlin { jvmToolchain(21) }
