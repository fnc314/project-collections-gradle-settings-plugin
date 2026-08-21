package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

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
public abstract class ProjectCollectionsGradleSettingsPlugin : Plugin<Settings> {

  /**
   * Returns `true` if `this` [File] is *NOT* the `build` directory
   * @receiver A [File] instance
   */
  private val File.isNotBuildDir: Boolean
    get() = isDirectory && name.equals("build").not()

  /**
   * Determines if `this` [File] [File.isNotBuildDir] holds and there is
   *   either a `build.gradle` or `build.gradle.kts` [File] therein
   * @receiver A [File] instance
   */
  private val File.containsGradleBuildFile: Boolean
    get() = isDirectory and (resolve("build.gradle").exists() or resolve("build.gradle.kts").exists())

  /**
   * Performs iterative checks against receiving [File] ensuring [File.isDirectory] and
   *   that the [File.name] *does not* start with `"_"` or `"."`
   * @receiver A [File] instance
   * @param fileSpec A [Spec] accepting a [File] for determining eligibility
   * @return `true` if [File] is eligible for [Settings.include]
   */
  private fun File.satisfiesGradleInclusionAndSpec(fileSpec: Spec<File>): Boolean =
    isNotBuildDir and
    containsGradleBuildFile and
    fileSpec.isSatisfiedBy(this)

  /**
   * Reduces this [File] to a [List] of [File]s which represent a collection of [File]s
   *   for which [satisfiesGradleInclusionAndSpec] is `true`
   * @receiver A [File] instance
   * @param nesting An [Int] representing the number of iterations of [Iterable.flatMap]
   *   required to fully expand this particular [File].  Default is `1`
   * @param fileSpec A [Spec] accepting a [File] for determining eligibility
   * @returns A [List] of [File]s qualifying for [Settings.include] invocations
   * @see satisfiesGradleInclusionAndSpec
   * @see isNotBuildDir
   */
  private fun File.gradleProjectFiles(nesting: Int = 1, fileSpec: Spec<File>): List<File> {
    if (nesting <= 0) {
      return emptyList()
    }

    val subDirs = listFilesOrdered { it.isNotBuildDir && fileSpec.isSatisfiedBy(it) }
    val projects = subDirs.filter { it.satisfiesGradleInclusionAndSpec(fileSpec) }
    val deeperProjects = subDirs.flatMap { it.gradleProjectFiles(nesting - 1, fileSpec) }

    return (projects + deeperProjects).distinct()
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

  /**
   * The necessary [Plugin.apply] method override
   * @param target A [Settings] object instance
   */
  override fun apply(target: Settings) {
    target.extensions.create(
      ProjectCollectionsGradleSettingsExtension::class.java,
      ProjectCollectionsGradleSettingsExtension.EXTENSION_NAME,
      ProjectCollectionsGradleSettingsExtensionImpl::class.java,
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