val serializationVersion = "1.8.0"
val ktorVersion = "3.1.0"
val pre = 841
val kotlinWrappersVersion = "1.0.0-pre.$pre"
val kotlinw = "org.jetbrains.kotlin-wrappers:kotlin"

plugins {
    kotlin("multiplatform") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }

    js(IR) {
        browser {
            binaries.executable()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-js:$ktorVersion")
                implementation("io.ktor:ktor-client-json-js:$ktorVersion")
                implementation("io.ktor:ktor-client-serialization-js:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation(project.dependencies.enforcedPlatform("$kotlinw-wrappers-bom:$kotlinWrappersVersion"))
                implementation("$kotlinw-react")
                implementation("$kotlinw-react-dom")
                implementation("$kotlinw-emotion")
                if (pre > 638) {
                    implementation("$kotlinw-mui-base")
                    implementation("$kotlinw-mui-material")
                    implementation("$kotlinw-mui-icons-material")
                } else {
                    implementation("$kotlinw-mui")
                    implementation("$kotlinw-mui-icons")
                }

                implementation(npm("date-fns", "4.1.0"))
                implementation(npm("@date-io/date-fns", "3.0.0"))
                implementation(npm("react-share", "~5.1.1"))
                implementation(npm("mui-nested-menu", "~3.4.0"))
                implementation(npm("ag-grid-community", "~35.3.1"))
                implementation(npm("ag-grid-react", "~35.3.1"))
            }
        }
    }
}

// Alias webpack config
tasks.withType<KotlinWebpack>().configureEach {
    dependsOn(":java:processResources")
}
