package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.internal

import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectCollectionsRecordTest {

  @field:TempDir
  lateinit var tempDir: File

  @Test
  fun testProjectCollectionsRecordProperties() {
    val projectDir = tempDir.resolve("features/auth")
    val record = ProjectCollectionsRecord(
      projectFile = projectDir,
      projectPath = ":features:auth",
      projectName = ":features-auth"
    )

    assertEquals(projectDir, record.projectFile)
    assertEquals(":features:auth", record.projectPath)
    assertEquals(":features-auth", record.projectName)
  }

  @Test
  fun testApplyToConfiguresSettings() {
    val projectDir = tempDir.resolve("features/auth").apply { mkdirs() }
    val record = ProjectCollectionsRecord(
      projectFile = projectDir,
      projectPath = ":features:auth",
      projectName = ":features-auth"
    )

    var includedProject: String? = null
    var requestedProject: String? = null
    var setProjectDir: File? = null

    val projectDescriptorHandler = InvocationHandler { _, method, args ->
      when (method.name) {
        "setProjectDir" -> {
          setProjectDir = args[0] as File
          null
        }
        "getProjectDir" -> setProjectDir
        "getName" -> "features-auth"
        else -> null
      }
    }

    val mockDescriptor = Proxy.newProxyInstance(
      ProjectDescriptor::class.java.classLoader,
      arrayOf(ProjectDescriptor::class.java),
      projectDescriptorHandler
    ) as ProjectDescriptor

    val settingsHandler = InvocationHandler { _, method, args ->
      when (method.name) {
        "include" -> {
          val names = args[0] as Array<*>
          includedProject = names.firstOrNull()?.toString()
          null
        }
        "project" -> {
          requestedProject = args[0] as String
          mockDescriptor
        }
        else -> null
      }
    }

    val mockSettings = Proxy.newProxyInstance(
      Settings::class.java.classLoader,
      arrayOf(Settings::class.java),
      settingsHandler
    ) as Settings

    record.applyTo(mockSettings)

    assertEquals(":features-auth", includedProject)
    assertEquals(":features-auth", requestedProject)
    assertEquals(projectDir, setProjectDir)
  }
}
