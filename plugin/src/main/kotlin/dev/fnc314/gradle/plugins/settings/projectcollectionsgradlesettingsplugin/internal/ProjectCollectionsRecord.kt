package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.internal

import org.gradle.api.initialization.Settings
import java.io.File

/**
 * Instances of this class are created as the [dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsExtension.projectCollections]
 *   is traversed and tested before calls to [Settings.include]
 *   are made.  The `projectFile` points to the actual [File] being referenced, whereas [projectPath]
 *   refers to the [org.gradle.api.invocation.Gradle]-based paths (using `:` separators).  The
 *   [projectName] comes from passing [projectPath] into [dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsExtension.projectNameTransform]
 *   so users can reduce the use of `:` in a deeply nested subproject.
 * @param projectFile The on-disk [File] being included
 * @param projectPath The `:`-delimited [org.gradle.api.invocation.Gradle] [org.gradle.api.Project] paths
 * @param projectName The massaged name for the [org.gradle.api.Project] normally found at [projectPath]
 */
internal data class ProjectCollectionsRecord(
  val projectFile: File,
  val projectPath: String,
  val projectName: String,
) {
  /**
   * Calls [Settings.include] and modifies the [Settings.project] reference to align
   *   with the [projectName] defined by the user
   * @param settings A [Settings] instance by which [org.gradle.api.Project]s are
   *   included
   */
  fun applyTo(settings: Settings) {
    settings.include(projectName)
    settings.project(projectName).projectDir = projectFile
  }
}
