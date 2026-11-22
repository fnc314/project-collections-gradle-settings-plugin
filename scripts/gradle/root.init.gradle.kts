@file:Suppress("UnstableApiUsage")

/**
 * Configures the receiving [RepositoryHandler] to include the GitHub `maven` repository
 * @receiver A [RepositoryHandler] instance
 * @param repoName A [String] refering to the particular `GitHub` repository
 */
private fun RepositoryHandler.addGithubPackageUrl(repoName: String) {
  val actor = System.getenv("GITHUB_ACTOR") ?: System.getenv("USER") ?: "fnc314"
  val token = System.getenv("GITHUB_TOKEN") ?: ""
  if (token.isNotEmpty()) {
    maven {
      url = uri("https://maven.pkg.github.com/$actor/$repoName")
      credentials {
        username = actor
        password = token
      }
      mavenContent {
        includeGroupAndSubgroups("com.$actor")
        includeGroupAndSubgroups("dev.$actor")
      }
    }
  }
}

/**
 * Adds the local `maven` repository to the receiving [RepositoryHandler]
 * @receiver A [RepositoryHandler] instance
 */
private fun RepositoryHandler.addMavenLocal() {
  val actor = System.getenv("GITHUB_ACTOR") ?: System.getenv("USER") ?: "fnc314"
  mavenLocal {
    mavenContent {
      includeGroupAndSubgroups("com.$actor")
      includeGroupAndSubgroups("dev.$actor")
    }
  }
}

/**
 * Applies the default repositories to the receiving [RepositoryHandler]
 * @receiver A [RepositoryHandler] instance
 */
private fun RepositoryHandler.addDefaultRepos() {
  google {
    mavenContent {
      includeGroupAndSubgroups("androidx")
      includeGroupAndSubgroups("com.android")
      includeGroupAndSubgroups("com.google")
    }
  }
  mavenCentral()
  gradlePluginPortal()
}

/** Value used for cache settings */
private val CACHE_DAYS: Int = 7

gradle.beforeSettings {
  enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

  caches {
    markingStrategy = MarkingStrategy.CACHEDIR_TAG
    cleanup = Cleanup.DEFAULT
    buildCache.setRemoveUnusedEntriesAfterDays(CACHE_DAYS)
    downloadedResources.setRemoveUnusedEntriesAfterDays(CACHE_DAYS)
    createdResources.setRemoveUnusedEntriesAfterDays(CACHE_DAYS)
  }

  buildCache {
    val rootDir = layout.settingsDirectory.asFile.name

    local {
      isPush = true
      isEnabled = true
      directory = gradle.gradleUserHomeDir.resolve("build-caches")
    }
  }

  pluginManagement {
    repositories {
      addDefaultRepos()
    }
  }

  dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
      addDefaultRepos()
    }
  }

  logger.error(
    """
    Settings Dir ${layout.settingsDirectory.asFile.absoluteFile}
    Root Dir ${layout.rootDirectory.asFile.absoluteFile.name}
    Parent? ${gradle.parent}
    Parent Gradle ${gradle.parent?.gradle}
    """
  )
}
