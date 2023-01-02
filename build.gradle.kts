import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "io.github.scottpierce.kotlin-node-slim"
version = "0.0.4"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")

    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // Ensure compatibility with old Gradle versions running Kotlin 1.4.
        // When changing this value see kotlinCompatibility.kt and delete unnecessary backports.
        apiVersion = "1.4"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    website.set("https://github.com/ScottPierce/kotlin-node-slim")
    vcsUrl.set("https://github.com/ScottPierce/kotlin-node-slim")
    plugins {
        create("kotlin-node-slim") {
            id = "io.github.scottpierce.kotlin-node-slim"
            implementationClass = "io.github.scottpierce.gradle.kotlin.node.slim.KotlinNodeSlimPlugin"
            displayName = "Kotlin Node Slim"
            description = "When releasing a module in Kotlin JS, all dependencies from all other js projects are " +
                    "included in the node_modules directory. This adds significant bloat when trying to package a " +
                    "Kotlin nodejs application in a docker container, or ship it as a standalone application.\n" +
                    "\n" +
                    "This plugin iterates over the dependencies of the module recursively, collects all referenced " +
                    "npm dependencies, and then generates a small npm project."
            tags.set(listOf("kotlin", "js", "kotlin-js", "kotlin-node", "node", "release", "slim", "slim release"))
        }
    }
}
