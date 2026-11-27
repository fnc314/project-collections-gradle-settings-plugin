package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProjectCollectionsExtensionTest {

  @Test
  fun testExtensionDefaultsAndInitialState() {
    val project = ProjectBuilder.builder().build()
    val extension: ProjectCollectionsGradleSettingsExtension = project.objects.newInstance(
      ProjectCollectionsGradleSettingsExtensionImpl::class.java
    )

    // Verify defaults
    assertTrue(extension.flattenMiddleProjects.get())
    assertNotNull(extension.fileSpec.get())
    assertNotNull(extension.projectNameTransform.get())
    assertTrue(extension.projectCollections.get().isEmpty())
  }

  @Test
  fun testFileSpecFilteringCriteria() {
    val project = ProjectBuilder.builder().build()
    val extension: ProjectCollectionsGradleSettingsExtension = project.objects.newInstance(
      ProjectCollectionsGradleSettingsExtensionImpl::class.java
    )
    val defaultSpec = extension.fileSpec.get()

    // Default filters out files starting with '_', '.', and '-'
    assertTrue(defaultSpec.isSatisfiedBy(File("components")))
    assertTrue(defaultSpec.isSatisfiedBy(File("design-system")))
    assertTrue(defaultSpec.isSatisfiedBy(File("features")))
    assertFalse(defaultSpec.isSatisfiedBy(File("_internal")))
    assertFalse(defaultSpec.isSatisfiedBy(File(".git")))
    assertFalse(defaultSpec.isSatisfiedBy(File("-temp")))

    // Verify custom fileSpec can be configured
    extension.fileSpec.set { file -> file.name != "scratch" && file.name != "drafts" }
    val customSpec = extension.fileSpec.get()
    assertTrue(customSpec.isSatisfiedBy(File("checkout")))
    assertFalse(customSpec.isSatisfiedBy(File("scratch")))
    assertFalse(customSpec.isSatisfiedBy(File("drafts")))
  }

  @Test
  fun testDslRegistrationMethods() {
    val project = ProjectBuilder.builder().build()
    val extension: ProjectCollectionsGradleSettingsExtension = project.objects.newInstance(
      ProjectCollectionsGradleSettingsExtensionImpl::class.java
    )

    with(extension) {
      "features" toDepthOf 3
      registerProjectCollection("components") // defaults to depth 1
      registerProjectCollection("libraries", 2)
      registerNestedProjectCollection("design-system", 4)
    }

    val registered = extension.projectCollections.get()
    assertEquals(3, registered["features"])
    assertEquals(1, registered["components"])
    assertEquals(2, registered["libraries"])
    assertEquals(4, registered["design-system"])
  }
}
