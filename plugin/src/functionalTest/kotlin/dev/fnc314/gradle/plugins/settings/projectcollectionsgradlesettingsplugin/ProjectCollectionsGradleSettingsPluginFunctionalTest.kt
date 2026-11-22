package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectCollectionsGradleSettingsPluginFunctionalTest {

  @field:TempDir
  lateinit var projectDir: File

  private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

  private fun setupNestedModules() {
    // Top-level feature-a
    projectDir.resolve("features/feature-a").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// feature a")
    }

    // Nested feature-b
    projectDir.resolve("features/nested/feature-b").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// feature b")
    }

    // Directory without build.gradle.kts (should be skipped as a project)
    projectDir.resolve("features/not-a-project").apply {
      mkdirs()
    }

    // Hidden directory (should be skipped by default fileSpec)
    projectDir.resolve("features/_ignored/feature-c").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// ignored feature")
    }
  }

  @Test
  fun testFlattenedProjectsInclusion() {
    setupNestedModules()

    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin")
      }

      rootProject.name = "test-flattened"

      projectCollections {
        "features" toDepthOf 2
      }
      """.trimIndent()
    )

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .withArguments("projects")
      .build()

    val output = result.output
    assertTrue(output.contains("Project ':features-feature-a'"), "Expected ':features-feature-a' in output:\n$output")
    assertTrue(output.contains("Project ':features-nested-feature-b'"), "Expected ':features-nested-feature-b' in output:\n$output")
    assertTrue(!output.contains("feature-c"), "Expected '_ignored/feature-c' to be excluded:\n$output")
  }

  @Test
  fun testUnflattenedProjectsInclusion() {
    setupNestedModules()

    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin")
      }

      rootProject.name = "test-unflattened"

      projectCollections {
        flattenMiddleProjects.set(false)
        "features" toDepthOf 2
      }
      """.trimIndent()
    )

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .withArguments("projects")
      .build()

    val output = result.output
    assertTrue(output.contains("Project ':features:feature-a'"), "Expected ':features:feature-a' in output:\n$output")
    assertTrue(output.contains("Project ':features:nested:feature-b'"), "Expected ':features:nested:feature-b' in output:\n$output")
  }

  @Test
  fun testCustomProjectNameTransformWithCollection() {
    setupNestedModules()

    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin")
      }

      rootProject.name = "test-custom-naming"

      projectCollections {
        projectNameTransform.set { collection, projectPath, _ ->
          // Custom: strip collection name from prefix and prefix with 'module-'
          val base = projectPath.removePrefix(":" + collection + ":").replace(":", "-")
          ":module-" + base
        }
        "features" toDepthOf 2
      }
      """.trimIndent()
    )

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .withArguments("projects")
      .build()

    val output = result.output
    assertTrue(output.contains("Project ':module-feature-a'"), "Expected ':module-feature-a' in output:\n$output")
    assertTrue(output.contains("Project ':module-nested-feature-b'"), "Expected ':module-nested-feature-b' in output:\n$output")
  }
}
