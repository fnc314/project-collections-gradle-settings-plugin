/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 * For more detailed information on multi-project builds, please refer to https://docs.gradle.org/8.14.3/userguide/multi_project_builds.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */
pluginManagement {
    includeBuild("../.")

    plugins {
        id("com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version("1.0.0")
    }

}

plugins {
    id("com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version("1.0.0")
}

projectCollections {
    listOf(
        "components",
        "design-system",
    ).onEach { it toDepthOf 1 }

    "features" toDepthOf 3
}

rootProject.name = "project-collections-gradle-settings-plugin-sample"
