package io.github.scottpierce.gradle.kotlin.node.slim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmDependency
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

private const val NODE_PROJECT_COMPILED_FILES_DIR = "/compileSync/main/productionExecutable/kotlin"
private const val NODE_JS_PRODUCTION_COMPILE_TASK = "compileProductionExecutableKotlinJs"

internal fun createCompileDistributionTask(target: Project, extension: KotlinNodeSlimPluginExtension) {
    val compileOutputDir = File(target.buildDir, NODE_PROJECT_COMPILED_FILES_DIR)
    val mainScript = "kotlin/${target.rootProject.name}-${target.name}.js"

    target.tasks.create("compileNodeJsSlimDistribution") { task ->
        val outputDir = extension.outputDir.get().asFile

        target.checkForMultiplatformPlugin()

        val prodCompileTask = target.tasks.getByName(NODE_JS_PRODUCTION_COMPILE_TASK)
            ?: throw IllegalStateException(
                """
                |Failed to find the task 'compileProductionExecutableKotlinJs'. This can happen for a few reasons:
                |
                |1. You don't have IR compilation enabled. The ${KotlinNodeSlimPlugin.NAME} only works with 
                |       IR compilations. You can enable the IR JS compiler by setting `kotlin.js.compiler=ir` in your 
                |       `gradle.properties` file.
                |2. You don't have a JS target applied to this project.
                |3. Your JS target doesn't have NodeJS compilation enabled.
                |4. You don't have your executable binary turned on.
                |
                |A minimally correct target configuration would looks something like this:
                |```
                |kotlin {
                |    js {
                |        nodejs()
                |        binaries.executable()
                |    }
                |}
                |```
                """.trimMargin()
            )

        task.dependsOn(prodCompileTask)

        task.doLast {
            val dependencies = mutableSetOf<ResolvedNpmDep>()
            val scannedProjectPaths = mutableSetOf<String>()
            val projectsToScan = LinkedList<Project>()

            projectsToScan += target
            while (projectsToScan.isNotEmpty()) {
                val project = projectsToScan.removeAt(0)
                val projectPath = project.path

                if (scannedProjectPaths.contains(projectPath)) {
                    continue
                }
                scannedProjectPaths += projectPath

                val result = project.scanDependencies()
                projectsToScan += result.projectDependencies.map { it.dependencyProject }
                dependencies += result.npmDependencies
            }

            outputDir.listFiles()?.forEach { it.deleteRecursively() }
            outputDir.mkdirs()
            File(outputDir, "package.json").apply {
                writeText(
                    generatePackageDotJson(
                        target = target,
                        mainScript = mainScript,
                        deps = dependencies.toList(),
                        scripts = extension.scripts.get() ?: mapOf()
                    )
                )
            }

            val kotlinOutputDir = File(outputDir, "kotlin")
            kotlinOutputDir.mkdirs()

            compileOutputDir
                .listFiles()!!
                .forEach {
                    val toFile = File(kotlinOutputDir, it.name)
                    Files.copy(it.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }

            val yarnLockFile = File(target.rootProject.buildDir, "js/yarn.lock").toPath()
            Files.copy(yarnLockFile, File(outputDir, "yarn.lock").toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}

private fun generatePackageDotJson(
    target: Project,
    mainScript: String,
    deps: List<ResolvedNpmDep>,
    scripts: Map<String, String>
): String {
    val sortedDeps = deps.sortedBy { it.name }

    val depsString = genDependenciesString(sortedDeps.filter { it.scope == NpmDependency.Scope.NORMAL })
    val devDepsString = genDependenciesString(sortedDeps.filter { it.scope == NpmDependency.Scope.DEV })
    val optionalDepsString = genDependenciesString(sortedDeps.filter { it.scope == NpmDependency.Scope.OPTIONAL })
    val peerDepsString = genDependenciesString(sortedDeps.filter { it.scope == NpmDependency.Scope.PEER })

    var versionString = target.version.toString()
    if (versionString == "unspecified") {
        versionString = "0.0.0-unspecified"
    }

    val sortedScripts = scripts.toList().sortedBy { it.first }
    val scriptsString = genScriptsString(sortedScripts, mainScript)

    return """
    |{
    |  "name": "${target.rootProject.name}-${target.name}",
    |  "version": "$versionString",
    |  "license": "UNLICENSED",
    |  "main": "$mainScript",
    |  "scripts": {${if (scriptsString.isNotBlank()) "\n$scriptsString" else ""}
    |  },
    |  "dependencies": {${if (depsString.isNotBlank()) "\n$depsString" else ""}
    |  },
    |  "devDependencies": {${if (devDepsString.isNotBlank()) "\n$devDepsString" else ""}
    |  },
    |  "optionalDependencies": {${if (optionalDepsString.isNotBlank()) "\n$optionalDepsString" else ""}
    |  },
    |  "peerDependencies": {${if (peerDepsString.isNotBlank()) "\n$peerDepsString" else ""}
    |  }
    |}
    """.trimMargin()
}

private fun genDependenciesString(deps: List<ResolvedNpmDep>): String {
    return buildString {
        for (i in deps.indices) {
            val dep = deps[i]

            append("    \"")
            append(dep.name)
            append("\": \"")
            append(dep.version)
            append('"')

            if (i != deps.lastIndex) {
                append(',')
                appendLine()
            }
        }
    }
}

private fun genScriptsString(scripts: List<Pair<String, String>>, mainScript: String): String {
    return buildString {
        for (i in scripts.indices) {
            val script = scripts[i]

            append("    \"")
            append(script.first)
            append("\": \"")
            append(script.second.replace("{{main}}", mainScript))
            append('"')

            if (i != scripts.lastIndex) {
                append(',')
                appendLine()
            }
        }
    }
}
