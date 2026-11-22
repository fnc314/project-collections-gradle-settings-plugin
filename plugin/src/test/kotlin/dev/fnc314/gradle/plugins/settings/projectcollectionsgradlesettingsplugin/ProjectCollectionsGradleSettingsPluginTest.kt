package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectCollectionsGradleSettingsPluginTest {

  @Test
  fun testExtensionDefaultsAndDsl() {
    val project = ProjectBuilder.builder().build()
    val extension: ProjectCollectionsGradleSettingsExtension = project.objects.newInstance(
      ProjectCollectionsGradleSettingsExtensionImpl::class.java
    )

    // Check defaults
    assertTrue(extension.flattenMiddleProjects.get())
    assertNotNull(extension.fileSpec.get())
    assertNotNull(extension.projectNameTransform.get())

    // Test default fileSpec filters out '_', '.', and '-'
    val spec = extension.fileSpec.get()
    assertTrue(spec.isSatisfiedBy(File("feature-a")))
    assertTrue(spec.isSatisfiedBy(File("components")))
    assertFalse(spec.isSatisfiedBy(File("_hidden")))
    assertFalse(spec.isSatisfiedBy(File(".git")))
    assertFalse(spec.isSatisfiedBy(File("-invalid")))

    // Test default projectNameTransform
    val defaultMapper = extension.projectNameTransform.get()
    assertEquals(
      ":features-feature-a",
      defaultMapper(collection = "features", projectPath = ":features:feature-a", flatten = true)
    )
    assertEquals(
      ":features:feature-a",
      defaultMapper(collection = "features", projectPath = ":features:feature-a", flatten = false)
    )

    // Test DSL registration functions
    with(extension) {
      "features" toDepthOf 2
      registerProjectCollection("components", 1)
      registerNestedProjectCollection("design-system", 3)
    }

    val collections = extension.projectCollections.get()
    assertEquals(2, collections["features"])
    assertEquals(1, collections["components"])
    assertEquals(3, collections["design-system"])
  }
}
