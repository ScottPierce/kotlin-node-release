import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    id("com.gradle.plugin-publish") version "1.1.0"
}

group = "io.github.scottpierce.kotlin-node-slim"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.21")

    testImplementation(kotlin("test"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    website.set("https://github.com/ScottPierce/kotlin-node-slim")
    vcsUrl.set("https://github.com/ScottPierce/kotlin-node-slim")
    plugins {
        create("kotlin-node-slim") {
            id = "io.github.scottpierce.kotlin-node-slim"
            implementationClass = "io.github.scottpierce.gradle.kotlin.node.slim.KotlinNodeSlimPlugin"
            displayName = "Kotlin Node Slim"
            description = "Creates a slim nodejs project of only the dependencies used by a Kotlin Node JS Gradle " +
                    "module so that only the used dependencies can be packaged. This helps to greatly reduce the " +
                    "released file size, especially in projects with multiple javascript projects."
            tags.set(listOf("kotlin", "js", "node", "release"))
        }
    }
}
