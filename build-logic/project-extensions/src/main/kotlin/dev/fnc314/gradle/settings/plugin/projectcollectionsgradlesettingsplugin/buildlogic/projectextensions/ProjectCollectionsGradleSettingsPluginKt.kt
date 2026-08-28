package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.embeddedKotlinVersion
import org.gradle.kotlin.dsl.getByType
import kotlin.jvm.optionals.getOrElse

/**
 * A [Directory] pointing to `/docs`
 * @receiver A [Project] instance
 */
val Project.dokkaDocsDirectory: Directory
  get() = rootProject.layout.projectDirectory.dir("docs")

/**
 * A [Provider] of a [Directory] into which `dokka` will place artifacts before final publication to
 *   [dokkaDocsDirectory], uses `build/dokka`
 * @receiver A [Project] instance
 */
val Project.dokkaDocsIntermediateDirectory: Provider<Directory>
  get() = rootProject.layout.buildDirectory.dir("dokka")

/**
 * Reads the `kotlin` version declared in [libs] and mutates it via [substringBeforeLast]
 * @receiver A [Project] instance
 */
val Project.kotlinVersion: String
  get() = libs.findVersion("kotlin")
    .map { it.requiredVersion.substringBeforeLast(".") }
    .getOrElse { embeddedKotlinVersion.substringBeforeLast(".") }

/**
 * Retrieves the `build.gradle.kts` default `libs` helper [VersionCatalog]
 * @receiver A [Project] instance
 * @return The [VersionCatalog] named `libs`
 */
val Project.libs: VersionCatalog get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Modifies `this` [MavenPublication] with the provided [project] with conventional values
 * @receiver A [MavenPublication] instance
 * @param project The publishing [Project]
 * @param publicationName The [String] name of the [MavenPublication]
 */
fun MavenPublication.defaultConfigs(project: Project, publicationName: String) {
  artifactId = project.name
  groupId = project.group.toString()
  version = project.version.toString()

  project.logger.lifecycle("Publication $publicationName for Project ${project.name}")

  pom {
    name.set("Project Collections Gradle Settings Plugin")
    description.set("A Gradle Settings Plugin to streamline `include` calls to arbitrarily nested sub-directories")
    inceptionYear.set("2025")
    packaging = "jar"
    version = project.version.toString()
    url.set("https://www.fnc314.dev/${project.name}")
    developers {
      developer {
        id.set("fnc314")
        name.set("Franco N. Colaizzi")
        email.set("fnc314@fnc314.com")
      }
    }
    contributors {
      contributor {
        name.set("Franco N. Colaizzi")
        email.set("fnc314@fnc314.com")
        url.set("https://www.fnc314.dev")
      }
    }
    scm {
      url.set("https://github.com/fnc314/${project.name}")
    }
  }
}