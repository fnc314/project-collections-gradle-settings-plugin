@file:Suppress("UnstableApiUsage")
package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.plugins

import org.gradle.plugin.compatibility.compatibility

plugins {
  // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
  id("com.gradle.plugin-publish")
  `java-gradle-plugin`
  `maven-publish`

  // Apply the `signing` plugin and configure appropriately
  // https://docs.gradle.org/current/userguide/signing_plugin.html
  signing
}

gradlePlugin {
  website = "https://www.fnc314.dev/${project.name}"
  vcsUrl = "https://github.com/fnc314/${project.name}"
  isAutomatedPublishing = true
  // Define the plugin
  plugins.create("projectCollectionsGradleSettingsPlugin") {
    id = "dev.fnc314.gradle.plugins.settings.${project.name}"
    implementationClass = "dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsPlugin"
    tags = listOf("gradle settings", "settings plugin", "gradle settings plugin")
    description = "A plugin for `org.gradle.api.initialization.Settings` to streamline calls to `org.gradle.api.initialization.Settings.include` for arbitrarily nested sub-projects"
    displayName = "project-collections-gradle-settings-plugin"
    compatibility {
      features {
        configurationCache = true
      }
    }
  }
}

publishing {
  repositories {
    maven {
      name = "GitHubPackages"
      url = uri("https://maven.pkg.github.com/fnc314/${project.name}")
      credentials {
        username = providers.environmentVariable("GITHUB_ACTOR").get()
        password = providers.environmentVariable("GITHUB_TOKEN").get()
      }
    }
  }
}