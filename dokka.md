# Module project-collections-settings-gradle-plugin

## About

When applied to a `settings.gradle[.kts]` file, this `org.gradle.api.Plugin` streamlines calls to `org.gradle.api.initialization.Settings.include` for arbitrary collections of (nested) sub-`org.gradle.api.Project`s.

## Usage

See how to declare a dependency upon the plugin and consume/apply it.

### Usage - Plugin Dependency

The plugin is published to the [`Gradle` Plugin Portal](https://plugins.gradle.org/plugin/com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin).  Gradle should resolve the dependency automatically.

### Usage - Plugin Application

The plugin is applied in `settings.gradle[.kts]` files
```gradle
plugins {
    id("com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version("$latest_version")
}
```

## Source

The source code is available [on `GitHub`](https://github.com/fnc314/project-collections-gradle-settings-plugin).

# Package com.fnc314.gradle.plugins.settings

All `org.gradle.api.Plugin`s defined for `org.gradle.api.initialization.Settings` targets.