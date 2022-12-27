package io.github.scottpierce.gradle.kotlin.node.slim

import org.gradle.api.Project

internal fun Project.checkForMultiplatformPlugin() {
    val project: Project = this
    if (!project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
        throw IllegalStateException(
            "The 'org.jetbrains.kotlin.multiplatform', or kotlin(\"multiplatform\"), " +
                "plugin has not been applied to the project '${project.path}'. The '${KotlinNodeSlimPlugin.NAME}' " +
                "plugin requires the Kotlin multiplatform plugin in order to function."
        )
    }
}
