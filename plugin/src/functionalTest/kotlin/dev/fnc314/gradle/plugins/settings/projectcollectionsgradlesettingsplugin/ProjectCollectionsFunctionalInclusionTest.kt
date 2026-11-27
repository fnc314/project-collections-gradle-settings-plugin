package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ProjectCollectionsFunctionalInclusionTest {

  @field:TempDir
  lateinit var projectDir: File

  private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

  @Test
  fun testArbitraryDepthDiscoveryAndBuildFileRequirement() {
    // 3-level deep feature
    projectDir.resolve("services/core/auth/impl").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// auth impl")
    }

    // 2-level deep feature (shallower than declared depth 3, should still be discovered if build.gradle.kts exists)
    projectDir.resolve("services/api").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// api module")
    }

    // Directory without build file - must be ignored
    projectDir.resolve("services/docs").apply {
      mkdirs()
      resolve("README.md").writeText("# Not a gradle project")
    }

    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin")
      }

      rootProject.name = "arbitrary-depth-test"

      projectCollections {
        "services" toDepthOf 3
      }
      """.trimIndent()
    )

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .withArguments("projects")
      .build()

    val output = result.output
    assertTrue(output.contains("Project ':services-core-auth-impl'"), "Expected ':services-core-auth-impl':\n$output")
    assertTrue(output.contains("Project ':services-api'"), "Expected ':services-api':\n$output")
    assertTrue(!output.contains("Project ':services-docs'"), "Expected 'services-docs' to not be included as a project:\n$output")
  }

  @Test
  fun testFileSpecFilteringFunctional() {
    projectDir.resolve("modules/active").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// active")
    }
    projectDir.resolve("modules/deprecated-old").apply {
      mkdirs()
      resolve("build.gradle.kts").writeText("// deprecated")
    }

    settingsFile.writeText(
      """
      plugins {
        id("dev.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin")
      }

      rootProject.name = "filespec-test"

      projectCollections {
        fileSpec.set { !it.name.startsWith("deprecated") }
        "modules" toDepthOf 1
      }
      """.trimIndent()
    )

    val result = GradleRunner.create()
      .withPluginClasspath()
      .withProjectDir(projectDir)
      .withArguments("projects")
      .build()

    val output = result.output
    assertTrue(output.contains("Project ':modules-active'"), "Expected ':modules-active':\n$output")
    assertTrue(!output.contains("deprecated-old"), "Expected ':modules-deprecated-old' to be filtered out:\n$output")
  }
}
