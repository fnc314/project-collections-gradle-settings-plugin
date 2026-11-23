import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
  alias(libs.plugins.autonomousapps.dependency.analysis)
}

kotlin {
  jvmToolchain(
    libs.versions.jdk.map { it.toInt() }.get()
  )
  compilerOptions {
    jvmTarget = libs.versions.jdk.map { JvmTarget.fromTarget(it) }
    apiVersion = libs.versions.kotlin.map {
      KotlinVersion.fromVersion(it.substringBeforeLast("."))
    }
    languageVersion = libs.versions.kotlin.map {
      KotlinVersion.fromVersion(it.substringBeforeLast("."))
    }
  }
}

java {
  withJavadocJar()
  withSourcesJar()
}

dependencies {
  implementation(gradleApi())
  implementation(gradleKotlinDsl())
  implementation(projects.projectExtensions)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.publish.gradle.plugin)
  implementation(libs.bundles.dokka)
  implementation(libs.com.autonomousapps.dependency.analysis.plugin)
}