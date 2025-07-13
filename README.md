# Module project-collections-gradle-settings-plugin
# Package com.fnc314.gradle.settings.plugin

A `org.gradle.api.initialization.Settings` `org.gradle.api.Plugin` to streamline calls to `org.gradle.api.initialization.Settings.include` based on nested collections of sub-projects

## Setup

The plugin is published in the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin).

```gradle
// settings.gradle.kts
plugins {
    id("com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version("$latest_version")
}
```

Or, with a [Version Catalog](https://docs.gradle.org/current/userguide/version_catalogs.html)

```toml
[versions]
    project-collections-settings-plugin = "latest_version"

[libraries]
    project-collections-settings-plugin = { group = "com.fnc314.gradle.plugins.settings", name = "project-collections-gradle-settings-plugin", version.ref = "project-collections-settings-plugin" }

[plugins]
    project-collections-settings-plugin = { id = "com.fnc314.gradle.pluggins.settings.project-collections-gradle-settings-pluin", version.ref = "project-collections-settings-plugin" }
```

Used in a build-conventions plugin

```gradle
dependencies {
    runtimeOnly(libs.project.collections.settings.plugin)
}
```

### Usage

This `org.gradle.api.Plugin` targets `org.gradle.api.initialization.Settings` objects.  The goal is to streamline [the `Settings` script](https://docs.gradle.org/current/userguide/settings_file_basics.html#sec:settings_file_script) regarding the `org.gradle.api.initialization.Settings.include` invocations.

```gradle
// settings.gradle.kts
import java.io.File

plugins {
    id("com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version("$latest_version")
}

projectCollections {
    // select top-level directories and declare the arbitrary depth
    //   to which the plugin will draft paths for subprojects
    "sub-projects" toDepthOf 3

    fileChecker.set { file: File ->
        // Provide explicit logical checks against a [File] object
        //   *after* having verified [File.isDirectory()] is `true`
    }
}
```

Assuming a project structure similar to the following:

```
root-directory
    - settings.gradle.kts
    - build.gradle.kts
    - gradle/
        - ...
    - sub-projects/
        - first-level-1/
            - second-level-1/
                - third-level-1/
                    - build.gradle.kts
                - third-level-2/
                    - build.gradle.kts
            - second-level-2/
                - third-level-1
                    - build.gradle.kts
        - first-level-2/
            - second-level-1/
                - build.gradle.kts
                - third-level-1/
                    - build.gradle.kts

```

Then the set of `org.gradle.api.initialization.Settings.include`-d `org.gradle.api.Project`s will be

```gradle
include(":sub-projects:first-level-1:second-level-1:third-level-1")

include(":sub-projects:first-level-1:second-level-1:third-level-2")

include(":sub-projects:first-level-1:second-level-2:third-level-1")

include(":sub-projects:first-level-2:second-level-1")

include(":sub-projects:first-level-2:second-level-1:third-level-1")
```

### Purpose

This `org.gradle.api.Plugin` helps when repositories grow to a large set of nested, related `org.gradle.api.Project`s.  The common `features` bucket is what actually inspired the work here.

The `org.gradle.api.Plugin` is applied [on this sample `Kotlin Multiplatform Compose` sample repo](https://github.com/fnc314/fnc314-kmp/blob/sample/com-fnc314-gradle-plugins-settings/settings.gradle.kts#L43-L50).