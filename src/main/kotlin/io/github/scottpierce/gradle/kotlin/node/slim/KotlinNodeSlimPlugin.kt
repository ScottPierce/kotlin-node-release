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
        internal const val NAME = "kotlin-node-slim"
    }

    override fun apply(target: Project) {
        val extension = target.objects.newInstance(KotlinNodeSlimPluginExtension::class.java)
        target.extensions.add(KotlinNodeSlimPluginExtension::class.java, "kotlinNodeSlim", extension)

        target.afterEvaluate {
            createCompileDistributionTask(target, extension)
        }
    }
}

abstract class KotlinNodeSlimPluginExtension @Inject constructor(target: Project) {
    private val objects = target.objects

    /**
     * The directory to output the distributable
     */
    @get:Optional
    val outputDir: RegularFileProperty = objects.fileProperty()
        .fileValue(File(target.buildDir, "slimDistributable"))

    /**
     * Scripts to be added to the generated package.json.
     *
     * The value {{main}} will be replaced with the path to the primary executable js file name.
     */
    @get:Optional
    val scripts: MapProperty<String, String> = objects.mapProperty(String::class.java, String::class.java)
}
