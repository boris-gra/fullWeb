// Root build.gradle.kts - Version Catalog and Plugin Management
// Subprojects: main (JS/Frontend), java (JVM/Backend)

plugins {
    kotlin("multiplatform") version "2.1.0" apply false
    kotlin("jvm") version "2.1.0" apply false
//    application apply false
    kotlin("plugin.serialization") version "2.1.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

