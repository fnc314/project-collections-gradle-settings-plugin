package com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.specs.Spec
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.support.listFilesOrdered
import java.io.File
import java.nio.file.FileSystems

/**
 * A [Plugin] for [Settings] objects which streamlines the collection of projects included in
 *   strategically named directories
 */
internal abstract class ProjectCollectionsGradleSettingsPlugin: Plugin<Settings> {

    /**
     * Performs iterative checks against receiving [File] ensuring [File.isDirectory] and
     *   that the [File.name] *does not* start with `"_"` or `"."`
     * @receiver A [File] instance
     * @param fileSpec A [Spec] accepting a [File] for determining eligibility
     * @return `true` if [File] is eligible for [Settings.include]
     */
    private fun File.satisfiesGradleInclusionAndSpec(fileSpec: Spec<File>): Boolean =
        isDirectory and name.equals("build").not() and fileSpec.isSatisfiedBy(this)

    /**
     * Assumes this [File] is [File.isDirectory] and invokes [File.listFilesOrdered]
     *   with the use of [File.satisfiesGradleInclusionAndSpec] filtering
     * @receiver A [File] instance
     * @param fileSpec A [Spec] accepting a [File] for determining eligibility
     * @returns A [List] of [File]s which are eligible for [Settings.include] invocations
     * @see satisfiesGradleInclusionAndSpec
     */
    private fun File.expandIntoGradleProjects(fileSpec: Spec<File>): List<File> =
        listFilesOrdered { subFile -> subFile.satisfiesGradleInclusionAndSpec(fileSpec = fileSpec) }

    /**
     * Reduces this [File] to a [List] of [File]s which represent a collection of [File]s
     *   for which [satisfiesGradleInclusionAndSpec] is `true`
     * @receiver A [File] instance
     * @param nesting An [Int] representing the number of iterations of [Iterable.flatMap]
     *   required to fully expand this particular [File].  Default is `1`
     * @param fileSpec A [Spec] accepting a [File] for determining eligibility
     * @returns A [List] of [File]s qualifying for [Settings.include] invocations
     * @see satisfiesGradleInclusionAndSpec
     * @see expandIntoGradleProjects
     */
    private fun File.gradleProjectFiles(nesting: Int = 1, fileSpec: Spec<File>): List<File> {
        val projFiles: MutableList<File> = mutableListOf()
        var round = 0
        while (round in 0 ..< nesting) {
            if (projFiles.isEmpty()) {
                projFiles.addAll(expandIntoGradleProjects(fileSpec = fileSpec))
            } else {
                val flatMappedFiles = projFiles.flatMap { it.expandIntoGradleProjects(fileSpec = fileSpec) }
                projFiles.addAll(flatMappedFiles)
            }
            round++
        }
        return projFiles
    }

    /**
     * Converts this [List] of [File]s to a [List] of [String]s constructed for [Settings.include] calls
     * @receiver A [List] of [File]s
     * @param settingsDir The [File] of this [Settings] object
     * @returns A [List] of [String]s for [Settings.include]
     */
    private fun List<File>.toGradleSettingsIncludeFormats(
        settingsDir: File,
    ): List<String> = map {
        it.absolutePath
            .substringAfter(delimiter = settingsDir.absolutePath)
            .replace(oldValue = FileSystems.getDefault().separator, newValue = ":")
    }

    override fun apply(target: Settings) {
        target.extensions.create(
            ProjectCollectionsGradleSettingsExtension::class.java,
            ProjectCollectionsGradleSettingsExtension.EXTENSION_NAME,
            ProjectCollectionsGradleSettingsExtensionImpl::class.java
        )
        target.gradle.settingsEvaluated { settings ->
            settings.extensions.getByType<ProjectCollectionsGradleSettingsExtension>().run {
                projectCollections
                    .map {
                        it.flatMap { (dir, depth) ->
                            settings.settingsDir
                                .resolve(relative = dir)
                                .gradleProjectFiles(nesting = depth, fileSpec = fileSpec.get())
                                .toGradleSettingsIncludeFormats(settingsDir = settings.settingsDir)
                        }
                    }
                    .get()
                    .onEach { settings.include(it) }
            }
        }
    }
}