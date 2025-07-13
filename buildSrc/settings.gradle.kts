dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("./../gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "project-collections-gradle-settings-plugin-buildSrc"