dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("./../gradle/libs.versions.toml"))
    }
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

rootProject.name = "project-collections-gradle-settings-plugin-build-logic"