package com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
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
     * @param fileCheck A [java.util.function.Function] on [File] instances returning a
     *   [Boolean] to determine eligibility
     * @return `true` if [File] is eligible for [Settings.include]
     */
    private fun File.isEligibleForGradleInclusion(
        fileCheck: java.util.function.Function<File, Boolean> =
            java.util.function.Function { file -> file.name.first().toString() !in listOf("_", ".", "-",) }
    ): Boolean =
        isDirectory and name.equals("build").not() and fileCheck.apply(this)


    /**
     * Assumes this [File] is [File.isDirectory] and invokes [File.listFilesOrdered]
     *   with the use of [File.isEligibleForGradleInclusion] filtering
     * @receiver A [File] instance
     * @param fileCheck A nullable [java.util.function.Function] accepting on a [File]
     *   returning a [Boolean] for eligibility
     * @returns A [List] of [File]s which are eligible for [Settings.include] invocations
     * @see isEligibleForGradleInclusion
     * @see FileCheck
     */
    private fun File.expandIntoGradleProjects(
        fileCheck: FileCheck = null
    ): List<File> =
        listFilesOrdered { subFile ->
            when (fileCheck) {
                null -> subFile.isEligibleForGradleInclusion()
                else -> subFile.isEligibleForGradleInclusion(fileCheck = fileCheck)
            }
        }

    /**
     * Reduces this [File] to a [List] of [File]s which represent a collection of [File]s
     *   for which [isEligibleForGradleInclusion] is true
     * @receiver A [File] instance
     * @param nesting An [Int] representing the number of iterations of [Iterable.flatMap]
     *   required to fully expand this particular [File].  Default is 1
     * @param fileCheck A nullable [java.util.function.Function] accepting on a [File]
     *   returning a [Boolean] for eligibility
     * @returns A [List] of [File]s qualifying for [Settings.include] invocations
     * @see isEligibleForGradleInclusion
     * @see expandIntoGradleProjects
     * @see FileCheck
     */
    private fun File.gradleProjectFiles(nesting: Int = 1, fileCheck: FileCheck = null): List<File> {
        val projFiles: MutableList<File> = mutableListOf()
        var round = 0
        while (round in 0 ..< nesting) {
            if (projFiles.isEmpty()) {
                projFiles.addAll(expandIntoGradleProjects(fileCheck = fileCheck))
            } else {
                val flatMappedFiles = projFiles.flatMap { it.expandIntoGradleProjects(fileCheck = fileCheck) }
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
            .substringAfter(settingsDir.absolutePath)
            .replace(FileSystems.getDefault().separator, ":")
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
                    .get()
                    .onEach { (dir, depth) ->
                        settings.settingsDir
                            .resolve(dir)
                            .gradleProjectFiles(nesting = depth, fileCheck = fileCheck.orNull)
                            .toGradleSettingsIncludeFormats(settingsDir = settings.settingsDir)
                            .onEach { settings.include(it) }
                    }
            }
        }
    }
}