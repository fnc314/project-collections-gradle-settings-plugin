@file:[
  Suppress("UnstableApiUsage")
  OptIn(
    ExperimentalKotlinGradlePluginApi::class,
    InternalKotlinGradlePluginApi::class,
  )
]

import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.defaultConfigs
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.kotlinVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  // Apply the Kotlin JVM plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm)

  // Dokka
  alias(libs.plugins.dokka.config)

  // Publishing (and Signing)
  alias(libs.plugins.publishing)

  alias(libs.plugins.autonomousapps.dependency.analysis)
}

version = libs.versions.project.get()
group = "dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin"

kotlin {
  explicitApi()
  jvmToolchain(
    libs.versions.jdk.map { it.toInt() }.get()
  )
  compilerOptions {
    jvmTarget = libs.versions.jdk.map { JvmTarget.fromTarget(it) }
    apiVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(kotlinVersion)
    languageVersion = org.jetbrains.kotlin.gradle.dsl.KotlinVersion.fromVersion(kotlinVersion)
    verbose = true
    optIn.addAll(
      "kotlin.ExperimentalStdlibApi",
    )
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}

publishing {
  publications {
    register<MavenPublication>(name = "gpr") {
      from(components["kotlin"])

      artifact(tasks.named("dokkaHtmlJar"))
      artifact(tasks.named("dokkaJavadocJar"))
      artifact(tasks.named("javadocJar"))
      defaultConfigs(project = project, publicationName = "gpr")
    }

    register<MavenPublication>(name = "pluginMaven") {
      artifact(tasks.named("dokkaHtmlJar"))
      artifact(tasks.named("dokkaJavadocJar"))
      artifact(tasks.named("javadocJar"))
      defaultConfigs(project = project, publicationName = "pluginMaven")
    }
  }
}

afterEvaluate {
  publishing.publications.joinToString(separator = "\n") { it.name }.also {
    logger.error(it)
  }
}

val test = testing.suites.getByName<JvmTestSuite>("test") {
  // Use KotlinTest test framework
  useKotlinTest(libs.versions.kotlin.get())
}

// Create a new test suite
val functionalTest = testing.suites.register<JvmTestSuite>("functionalTest") {
  // Use KotlinTest test framework
  useKotlinTest(libs.versions.kotlin.get())

  dependencies {
    // functionalTest test suite depends on the production code in tests
    implementation(project())
  }

  targets {
    all {
      // This test suite should run after the built-in test suite has run its tests
      testTask.configure { shouldRunAfter(test) }
    }
  }
}

tasks {
  named<Task>("check") {
    // Include functionalTest as part of the check lifecycle
    dependsOn(functionalTest)
  }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

dependencies {
  compileOnly(gradleKotlinDsl())
  compileOnly(libs.kotlin)
  dokkaHtmlPlugin(libs.dokka.plugin.versioning)
}