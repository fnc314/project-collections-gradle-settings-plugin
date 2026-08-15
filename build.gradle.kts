plugins {
  // Apply the Kotlin JVM plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm) apply false

  // Dokka
  alias(libs.plugins.dokka.config) apply false

  // Publishing (and Signing)
  alias(libs.plugins.publishing) apply false

  alias(libs.plugins.autonomousapps.dependency.analysis) apply false
}

version = libs.versions.project.get()