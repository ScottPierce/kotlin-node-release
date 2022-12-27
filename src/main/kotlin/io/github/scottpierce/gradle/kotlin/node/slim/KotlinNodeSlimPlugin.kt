package io.github.scottpierce.gradle.kotlin.node.slim

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Optional
import java.io.File
import javax.inject.Inject

class KotlinNodeSlimPlugin : Plugin<Project> {
    companion object {
        internal const val NAME = "kotlin-node-release"
    }

    override fun apply(target: Project) {
        val extension = target.extensions.create("slimDistribution", KotlinNodeJsDistributionPluginExtension::class.java, target)

        target.afterEvaluate {
            createCompileDistributionTask(target, extension)
        }
    }
}

@Suppress("UnnecessaryAbstractClass")
abstract class KotlinNodeJsDistributionPluginExtension @Inject constructor(target: Project) {
    private val objects = target.objects

    @get:Optional
    val outputDir: RegularFileProperty = objects.fileProperty()
        .fileValue(File(target.buildDir, "slimDistributable"))

    @get:Optional
    val scripts: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
}
