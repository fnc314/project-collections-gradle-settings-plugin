# Project Collections Gradle Settings Plugin

A Gradle convention `Settings` plugin to streamline calls to `Settings.include` for arbitrarily nested multi-project architectures.

[![CI](https://github.com/fnc314/project-collections-gradle-settings-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/fnc314/project-collections-gradle-settings-plugin/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/version-5.4.0-blue.svg)](https://github.com/fnc314/project-collections-gradle-settings-plugin/releases)
[![Gradle Plugin Portal](https://img.shields.io/badge/plugin--portal-dev.fnc314.project--collections-brightgreen.svg)](https://plugins.gradle.org/plugin/dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin)

## Overview

As enterprise Gradle multi-project repositories grow, managing hundreds of manual `include(...)` statements across nested directories (`features`, `components`, `design-system`, `services`) quickly degrades into verbose, error-prone boilerplate.

The `project-collections-gradle-settings-plugin` automatically discovers and registers nested subprojects based on declarative depth traversal rules, while adhering strictly to **Gradle Best Practices** (such as avoiding empty intermediate container projects and remaining fully compatible with the Gradle Configuration Cache and Project Isolation).

### Key Capabilities

1. **Declarative Collection Inclusions**: Define top-level directory roots and specify traversal depth using concise Kotlin DSL:
   ```kotlin
   "features" toDepthOf 3
   "components" toDepthOf 1
   ```
2. **Empty Middle Project Flattening**: By default, converts deep paths like `features/auth/login` to `:features-auth-login` instead of `:features:auth:login`, eliminating unnecessary intermediate empty `Project` instances as recommended by [Gradle Structuring Best Practices](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html#avoid_empty_projects).
3. **Filtering & File Predicates (`fileSpec`)**: Filter directories dynamically with an optional `Spec<File>` predicate (e.g., skip `scratch` or hidden directories).
4. **Custom Name Transformation**: Supply a custom `ProjectNameMapper` to implement domain-specific naming conventions across your monorepo.
5. **Lowest JDK Compatibility Target**: Compiled targeting Java 17 bytecode, ensuring compatibility with any modern Gradle JVM running Java 17, 21, 25, or 26+.
6. **Configuration Cache & Project Isolation Friendly**: Backed by configuration-cache-safe Gradle properties (`MapProperty<String, Int>`, `Property<Boolean>`).

---

## Setup

Apply the plugin directly in your `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
plugins {
    id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version "5.4.0"
}
```

Or declare it within your version catalog:

```toml
# gradle/libs.versions.toml
[plugins]
project-collections = { id = "dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin", version = "5.4.0" }
```

And apply via alias in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
plugins {
    alias(libs.plugins.project.collections)
}
```

---

## Usage

Configure the `projectCollections` extension in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
import java.io.File

plugins {
    id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin") version "5.4.0"
}

projectCollections {
    // Traverse 'components' and 'design-system' 1 level deep
    listOf(
        "components",
        "design-system",
    ).onEach { it toDepthOf 1 }

    // Traverse 'features' 3 levels deep
    "features" toDepthOf 3

    // Optional: filter out directories starting with '_' or '.'
    fileSpec.set { file: File ->
        !file.name.startsWith("_") && !file.name.startsWith(".")
    }

    // Default is true: flattens ':features:auth:login' to ':features-auth-login'
    flattenMiddleProjects.set(true)
}

rootProject.name = "my-monorepo"
```

### Directory Traversal Example

Given the following project hierarchy:

```text
my-monorepo/
├── settings.gradle.kts
├── build.gradle.kts
├── components/
│   ├── button/
│   │   └── build.gradle.kts
│   └── card/
│       └── build.gradle.kts
└── features/
    └── checkout/
        └── payment/
            └── build.gradle.kts
```

With `flattenMiddleProjects.set(true)` (default), Gradle automatically includes:
* `:components-button` (pointing to `components/button`)
* `:components-card` (pointing to `components/card`)
* `:features-checkout-payment` (pointing to `features/checkout/payment`)

No empty intermediate projects (`:features` or `:features:checkout`) are created.

---

## Documentation

* **Official Landing Page**: [https://project-collections-gradle-settings-plugin.fnc314.dev](https://project-collections-gradle-settings-plugin.fnc314.dev)
* **API Documentation (Dokka)**: [API Reference](https://project-collections-gradle-settings-plugin.fnc314.dev/dokka/index.html)
* **Javadoc Reference**: [Javadoc](https://project-collections-gradle-settings-plugin.fnc314.dev/javadoc/index.html)

---

## License

[MIT License](LICENSE) &copy; 2026 Franco N. Colaizzi
