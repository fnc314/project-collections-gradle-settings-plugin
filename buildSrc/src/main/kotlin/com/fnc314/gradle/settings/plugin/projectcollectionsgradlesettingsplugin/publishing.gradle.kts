package com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    id("com.gradle.plugin-publish")

    // Apply the `signing` plugin and configure appropriately
    // https://docs.gradle.org/current/userguide/signing_plugin.html
    signing
}

gradlePlugin {
    website = "https://fnc314.com/${rootProject.name}"
    vcsUrl = "https://github.com/fnc314/${rootProject.name}"
    // Define the plugin
    val projectCollectionsGradleSettingsPlugin by plugins.creating {
        id = "com.fnc314.gradle.plugins.settings.${rootProject.name}"
        implementationClass = "com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsPlugin"
        tags = listOf("gradle settings", "settings plugin", "gradle settings plugin",)
        description = "A plugin for `org.gradle.api.initialization.Settings` to streamline calls to `org.gradle.api.initialization.Settings.include` for arbitrarily nested sub-projects"
        displayName = "project-collections-gradle-settings-plugin"
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fnc314/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}