package dev.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectNameMapperTest {

  @Test
  fun testDefaultProjectNameTransformFlattensCorrectly() {
    val mapper = ProjectNameMapper { _, projectPath, flatten ->
      if (flatten) {
        ":${projectPath.removePrefix(":").replace(":", "-")}"
      } else {
        projectPath
      }
    }

    val flattened = mapper(
      collection = "features",
      projectPath = ":features:auth:ui",
      flatten = true
    )
    assertEquals(":features-auth-ui", flattened)

    val unflattened = mapper(
      collection = "features",
      projectPath = ":features:auth:ui",
      flatten = false
    )
    assertEquals(":features:auth:ui", unflattened)
  }

  @Test
  fun testCustomProjectNameTransformUsingCollection() {
    val mapper = ProjectNameMapper { collection, projectPath, _ ->
      // Custom mapper strips collection prefix and prefixes with collection code
      val withoutCollection = projectPath.removePrefix(":$collection:")
      ":${collection.first()}-$withoutCollection"
    }

    val mapped = mapper(
      collection = "features",
      projectPath = ":features:checkout",
      flatten = true
    )
    assertEquals(":f-checkout", mapped)
  }
}
