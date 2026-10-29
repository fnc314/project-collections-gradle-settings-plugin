@file:Suppress("UnstableApiUsage")

package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.projectplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.plugin.compatibility.compatibility
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import java.net.URI
import javax.inject.Inject

abstract class ProjectPublishingPlugin @Inject constructor() : Plugin<Project> {
  override fun apply(target: Project) {
    target.pluginManager.apply("com.gradle.plugin-publish")
    target.pluginManager.apply("java-gradle-plugin")
    target.pluginManager.apply("maven-publish")
    target.pluginManager.apply("signing")

    target.configure<GradlePluginDevelopmentExtension> {
      website.set("https://www.fnc314.dev/${target.name}")
      vcsUrl.set("https://github.com/fnc314/${target.name}")
      plugins.create("projectCollectionsGradleSettingsPlugin") {
        id = "dev.fnc314.gradle.plugins.settings.${target.name}"
        implementationClass =
          "dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsPlugin"
        tags.set(listOf("gradle settings", "settings plugin", "gradle settings plugin"))
        description =
          "A plugin for `org.gradle.api.initialization.Settings` to streamline calls to `org.gradle.api.initialization.Settings.include` for arbitrarily nested sub-projects"
        displayName = "project-collections-gradle-settings-plugin"
        compatibility {
          features {
            configurationCache.set(true)
          }
        }
      }
    }

    target.configure<PublishingExtension> {
      repositories {
        maven {
          name = "GitHubPackages"
          url = URI.create("https://maven.pkg.github.com/fnc314/${target.name}")
          credentials {
            username = target.providers.environmentVariable("GITHUB_ACTOR").get()
            password = target.providers.environmentVariable("GITHUB_TOKEN").get()
          }
        }
      }
    }
  }
}
