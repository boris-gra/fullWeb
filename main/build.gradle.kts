import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

val serializationVersion = "1.11.0"
val ktorVersion = "3.5.0"
val kotlinWrappersVersion = "2026.6.10"
val kotlinw = "org.jetbrains.kotlin-wrappers:kotlin"

plugins {
    kotlin("multiplatform") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
}

repositories {
    mavenCentral()
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.2"
        }
    }

    js() {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        commonMain{
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
            }
        }

        jsMain{
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-json-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
                implementation(project.dependencies.enforcedPlatform("$kotlinw-wrappers-bom:$kotlinWrappersVersion"))
                implementation("$kotlinw-react")
                implementation("$kotlinw-react-dom")
                implementation("$kotlinw-mui-base")
                implementation("$kotlinw-mui-material")
                implementation("$kotlinw-mui-icons-material")
                implementation(npm("date-fns", "4.1.0"))
                implementation(npm("@emotion/react", "11.14.0"))
                implementation(npm("@emotion/styled", "11.14.1"))
                implementation(npm("@date-io/date-fns", "3.0.0"))
                implementation(npm("react-share", "~5.3.0"))
                implementation(npm("mui-nested-menu", "~3.4.0"))
                implementation(npm("ag-grid-community", "~36.0.0"))
                implementation(npm("ag-grid-react", "~36.0.0"))
            }
        }
    }
}

// Alias webpack config
tasks.withType<KotlinWebpack>().configureEach {
    dependsOn(":java:processResources")
}
