plugins {
  `kotlin-dsl`
  // alias(libs.plugins.kotlin.jvm) apply(false)
  alias(libs.plugins.autonomousapps.dependency.analysis) apply(false)
}

dependencies {
  runtimeOnly(
    libs.com.autonomousapps.dependency.analysis.plugin
  )
}