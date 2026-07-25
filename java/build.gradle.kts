//+++ java/build.gradle.kts (修改后)
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val ktorVersion = "3.5.0"
val serializationVersion = "1.11.0"

plugins {
    kotlin("jvm") version "2.4.10"
    application
    kotlin("plugin.serialization") version "2.4.10"
}

group = "org.example"
version = "1.2"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

kotlin {
    jvmToolchain(25)
}

sourceSets {
    main{
        kotlin.srcDir("src/jvmMain/kotlin")
        dependencies {
            implementation(kotlin("stdlib"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
//            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")

            implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
            implementation("io.ktor:ktor-server-compression:$ktorVersion")
            implementation("io.ktor:ktor-server-cors:$ktorVersion")
            implementation("io.ktor:ktor-serialization:$ktorVersion")
            implementation("io.ktor:ktor-server-core:$ktorVersion")
            implementation("io.ktor:ktor-server-netty:$ktorVersion")
            implementation("ch.qos.logback:logback-classic:1.5.12")
            implementation("io.ktor:ktor-websockets:$ktorVersion")
            implementation("org.postgresql:postgresql:42.7.4")
            implementation("com.google.cloud:google-cloud-bigquery:2.44.0")
        }
    }
}

application {
    mainClass.set("ServerKt")
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

distributions {
    main {
        contents {
            from("$projectDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}

tasks.register("stage") {
    dependsOn(tasks.getByName("installDist"))
}