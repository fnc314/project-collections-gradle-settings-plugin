dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./../gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "project-collections-gradle-settings-plugin-buildSrc"