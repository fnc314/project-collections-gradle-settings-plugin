package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

/**
 * A convenience name for a [String] to [String] callback used to convert
 *   a [org.gradle.api.invocation.Gradle] `projectPath` into an alternative,
 *   shorter, form.  This is to bring the existing plugin in line with `Gradle`
 *   best practices. See [Best Practices - Structure - Avoid Empty Projects](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#avoid_empty_projects)
 *   for more information.
 */
public typealias ProjectNameMapper = (projectPath: String) -> String;