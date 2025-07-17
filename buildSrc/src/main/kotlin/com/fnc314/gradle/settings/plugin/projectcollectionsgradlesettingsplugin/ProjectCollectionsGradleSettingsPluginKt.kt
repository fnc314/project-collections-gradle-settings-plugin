package com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.file.Directory
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getByType

/** A [Directory] pointing to [Project.getProjectDir]/docs */
val Project.dokkaDocsDirectory: Directory
    get() = rootProject.layout.projectDirectory.dir("docs")

val Project.dokkaKDocsDirectory: Directory
    get() = rootProject.layout.projectDirectory.dir("kdocs")

/** A [Directory] pointing to [Project.getProjectDir]/versioned-docs */
val Project.versionedDokkaDocsDirectory: Directory
    get() = rootProject.layout.projectDirectory.dir("versioned-docs")

/**
 * Retrieves the `build.gradle.kts` default `libs` helper [org.gradle.api.artifacts.VersionCatalog]
 * @receiver A [Project] instance
 * @return The [VersionCatalog] named `libs`
 */
private val Project.libs: VersionCatalog get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Modifies `this` [MavenPublication] with the provided [project] with conventional values
 * @receiver A [MavenPublication]
 * @param project The publishing [Project]
 */
fun MavenPublication.defaultConfigs(project: Project) {
    artifactId = project.rootProject.name
    groupId = project.rootProject.group.toString()
    version = project.rootProject.version.toString()

    pom {
        name.set("Project Collections Gradle Settings Plugin")
        description.set("A Gradle Settings Plugin to streamline `include` calls to arbitrarily nested sub-directories")
        inceptionYear.set("2025")
        packaging = "jar"
        version = project.rootProject.version.toString()
        url.set("https://fnc314.com/${project.rootProject.name}")
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
                url.set("https://fnc314.com")
            }
        }
        scm {
            url.set("https://github.com/fnc314/${project.rootProject.name}")
        }
        distributionManagement {
            downloadUrl.set("https://github.com/fnc314/${project.rootProject.name}/packages")
            relocation {
                artifactId.set(project.rootProject.name)
                groupId.set(project.rootProject.group.toString())
                version.set(project.rootProject.version.toString())
            }
        }
    }
}