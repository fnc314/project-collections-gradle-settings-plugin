package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

/**
 * A converter for [String] and [Boolean] to [String] callback used to convert
 *   a [org.gradle.api.invocation.Gradle] `projectPath` into an alternative,
 *   shorter, form.  This is to bring the existing plugin in line with `Gradle`
 *   best practices. See [Best Practices - Structure - Avoid Empty Projects](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#avoid_empty_projects)
 *   for more information.
 * @see <a href="https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#avoid_empty_projects">Gradle Best Practices - Structure - Avoid Empty Projects</a>
 */
public fun interface ProjectNameMapper {
  /**
   * Handles the mapping of a Gradle project path of the form
   *   `:some:project:path` including the collection directory name
   *   and the `Boolean` indicating whether middle projects should
   *   be flattened or preserved (i.e. should `:some` and `:project`
   *   be created in creating `:path`)
   *
   * ```kotlin
   * projectMapper = { collection: String, projectPath: String, flatten: Boolean ->
   *   // do something with `collection`, `projectPath`, and `flatten`
   *   return projectPath
   * }
   * ```
   *
   * @param collection the collection name/top-level directory
   * @param projectPath the project path
   * @param flatten
   * @return the string
   */
  public operator fun invoke(
    collection: String,
    projectPath: String,
    flatten: Boolean
  ): String
}