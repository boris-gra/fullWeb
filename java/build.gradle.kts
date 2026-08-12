import org.gradle.api.file.DuplicatesStrategy
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

import java.text.SimpleDateFormat
import java.util.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties
val ktorVersion = "3.5.2"
val serializationVersion = "1.11.0"

plugins {
    kotlin("jvm") version "2.4.10"
    application
    kotlin("plugin.serialization") version "2.4.10"
}

group = "org.example"
version = "1.4-2.4.10"

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
        resources.srcDir("src/jvmMain/resources")
        dependencies {
            implementation(kotlin("stdlib"))
            implementation(project(":shared"))
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
            implementation("ch.qos.logback:logback-classic:1.6.1")
            implementation("io.ktor:ktor-websockets:$ktorVersion")
            implementation("org.postgresql:postgresql:42.7.13")
            implementation("com.google.cloud:google-cloud-bigquery:2.69.0")
        }
    }
}

application {
    mainClass.set("ServerKt")
    // Name of the start scripts (bin/query-gra) matching the distribution and Docker/Procfile paths
    applicationName = "query-gra"
    myProp()
}

// Package the :main frontend build (main.js + index.html + css + img) into this server's
// classpath resources so the Ktor server can serve the whole application (frontend + API)
// on a single port. The server then serves the frontend via staticResources().
//
// Production  mode (installDist, build): uses jsBrowserProductionWebpack (minimized, optimized)
// Development mode (gradle run):         uses jsBrowserDevelopmentWebpack  (source maps, fast)
//

val isDevelopmentRun = gradle.startParameter.taskNames.any { it == ":java:run" }

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    if (isDevelopmentRun) {
        dependsOn(":main:jsBrowserDevelopmentWebpack")
        from(project(":main").layout.buildDirectory.dir("kotlin-webpack/js/developmentExecutable"))
        from(project(":main").layout.buildDirectory.dir("processedResources/js/main"))
    } else {
        dependsOn(":main:build")
        from(project(":main").layout.buildDirectory.dir("dist/js/productionExecutable"))
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

distributions {
    main {
        // Keep the historically used distribution/binary name (Dockerfiles, Procfile)
        distributionBaseName.set("query-gra")
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
            FileOutputStream("./java/src/jvmMain/resources/config.properties")
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