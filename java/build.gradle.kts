import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

import java.text.SimpleDateFormat
import java.util.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties
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
    myProp()
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

//tasks.named("processResources") {
//    dependsOn(":main:browserDevelopmentWebpack")
//    from(project(":main").layout.buildDirectory.dir("dist/js/developmentExecutable"))
//    into(layout.projectDirectory.dir("src/main/resources/static"))
//}


//https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code/41495542#41495542
fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
) = runCatching {
    ProcessBuilder("\\s".toRegex().split(this))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
}.onFailure { it.printStackTrace() }.getOrNull()
    ?.replace("\n", "") ?: ""

fun gitCommitHash() =
    "git rev-parse --verify --short HEAD"
        .runCommand()

fun lastNumberCommit() =
    "git rev-list --first-parent --count HEAD" // current HEAD commit
        .runCommand()
fun nodeVer() =
    "node -v".runCommand()
fun myProp() = // https://mkyong.com/java/java-properties-file-examples/
    try {
        if (version != "") {
            FileOutputStream("./java/src/jvmMain/kotlin/config.properties")
                .use { output ->
                    Properties()
                        .let {
                            it.setProperty(
                                "git.version",
                                "$version.${lastNumberCommit()}-${gitCommitHash()}"+
//                                        " node.${"node -v".runCommand()}" +
                                        " Compile on ${SimpleDateFormat("dd.MM.yyyy HH:mm")
                                            .format(Date())}"
                            )
                            it.store(output, null)
                            println(it.getProperty("git.version"))
                        }
                }
        } else ""
    } catch (e: IOException) {
        println("Gradle:my config.properties ERROR ${e.message}")
    }