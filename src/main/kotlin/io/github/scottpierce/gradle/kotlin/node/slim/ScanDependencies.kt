package io.github.scottpierce.gradle.kotlin.node.slim

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget
import org.jetbrains.kotlin.gradle.targets.js.npm.NpmDependency

internal fun Project.scanDependencies(): ScanResult {
    val project = this
    project.checkForMultiplatformPlugin()

    val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

    val nodeJsTargets: List<KotlinJsIrTarget> = kotlin.targets.toList()
        .filter { it is KotlinJsIrTarget && it.isNodejsConfigured }
        .map { it as KotlinJsIrTarget }

    if (nodeJsTargets.size > 1) {
        throw IllegalStateException(
            "Multiple possible NodeJS targets found in project '${project.path}'. " +
                "Please create an issue detailing your use case so that it might be properly supported."
        )
    } else if (nodeJsTargets.isEmpty()) {
        throw IllegalStateException("No NodeJS targets found for this project '${project.path}'.")
    }

    val nodeJsTarget = nodeJsTargets.first()
    val sourceSets = nodeJsTarget.compilations.getByName("main").allKotlinSourceSets

    val dependencies = sourceSets
        .flatMap { listOf(it.apiConfigurationName, it.implementationConfigurationName) }
        .mapNotNull { configurations.getByName(it) }
        .flatMap { it.dependencies }

    val projectDependencies = mutableListOf<ProjectDependency>()
    val npmDependencies = mutableListOf<ResolvedNpmDep>()
    for (dep in dependencies) {
        if (dep is ProjectDependency) {
            projectDependencies += dep
        } else if (dep is NpmDependency) {
            npmDependencies += ResolvedNpmDep(name = dep.name, version = dep.version, scope = dep.scope)
        }
    }

    return ScanResult(
        projectDependencies = projectDependencies,
        npmDependencies = npmDependencies
    )
}

internal data class ScanResult(
    val projectDependencies: List<ProjectDependency>,
    val npmDependencies: List<ResolvedNpmDep>
)

internal data class ResolvedNpmDep(
    val name: String,
    val version: String,
    val scope: NpmDependency.Scope
)
