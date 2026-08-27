package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.plugins.dokka

import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.extensions.dokkaDocsDirectory
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.extensions.dokkaDocsIntermediateDirectory
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.extensions.kotlinVersion
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.extensions.libs
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import kotlin.jvm.optionals.getOrDefault

plugins {
  kotlin("jvm")
  id("org.jetbrains.dokka")
  id("org.jetbrains.dokka-javadoc")
}

val projectVersion: String =
  libs.findVersion("project").map { it.requiredVersion }.getOrDefault("${rootProject.version}")

dokka {
  moduleName = project.name
  moduleVersion = projectVersion
  basePublicationsDirectory = dokkaDocsIntermediateDirectory
  modulePath = path
  sourceSetScopeDefault = path
  dokkaSourceSets {
    configureEach {
      sourceRoots = layout.projectDirectory.files("src/${this@configureEach.name}/kotlin")
      includes.from(
        rootProject.layout.projectDirectory.files(
          "README.md", "dokka.md"
        )
      )

      suppress = this@configureEach.name != "main"
      suppressGeneratedFiles = false
      enableJdkDocumentationLink = true
      enableKotlinStdLibDocumentationLink = true
      enableAndroidDocumentationLink = false
      reportUndocumented = true

      documentedVisibilities = setOf(VisibilityModifier.Public)
      analysisPlatform = KotlinPlatform.JVM
      languageVersion.set(kotlinVersion)
      apiVersion.set(kotlinVersion)
      jdkVersion.set(
        libs.findVersion("jdk")
          .map { it.requiredVersion.toInt() }
          .get()
      )

      sourceLink {
        localDirectory = layout.projectDirectory.dir("src")
        remoteUrl = uri("https://github.com/fnc314/${project.name}/tree/main/${project.path.replace(":", "")}/src")
        remoteLineSuffix = "#L"
      }

      perPackageOptions.all {
        documentedVisibilities = setOf(VisibilityModifier.Public)
        skipDeprecated = true
        reportUndocumented = true
      }

      externalDocumentationLinks.maybeCreate("gradle").apply {
        url = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc")
        packageListUrl = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc/element-list")
      }

      logger.error("DSS -> ${sourceSetId.get()}")
    }
  }

  pluginsConfiguration {
    html {
      homepageLink.value("https://www.fnc314.dev/${project.name}/")
      footerMessage.value(
        provider {
          buildString {
            append("(C) <a href=\"https://www.fnc314.dev\" target=\"_blank\">fnc314</a>")
            append(" | ")
            append("<a href=\"https://www.fnc314.dev/${project.name}/dokka\" target=\"_blank\">dokka</a>")
            append(" | ")
            append("<a href=\"https://www.fnc314.dev/${project.name}/javadoc\" target=\"_blank\">javadoc</a>")
            append(" | ")
            append(project.version)
          }
        }
      )
    }
    versioning {
      version = projectVersion
      renderVersionsNavigationOnAllPages = true
      olderVersionsDir = dokkaDocsDirectory.dir("versioned-dokka")
    }
  }

  dokkaPublications {
    configureEach {
      moduleName = project.name
      moduleVersion = projectVersion
      suppressInheritedMembers = true
      suppressObviousFunctions = true
      includes.from(
        rootProject.layout.projectDirectory.files(
          "README.md", "dokka.md"
        )
      )
    }
  }
}

val dokkaHtmlCapture = tasks.register<Sync>("dokkaHtmlCapture") {
  group = "dokka"
  description = "Syncs content from build/dokka into docs/dokka"
  from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
  into(dokkaDocsDirectory.dir("dokka"))
  destinationDir = dokkaDocsDirectory.dir("dokka").asFile
}

val dokkaJavadocCapture = tasks.register<Sync>("dokkaJavadocCapture") {
  group = "dokka"
  description = "Syncs content from build/dokka-javadoc into docs/javadoc"
  from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
  into(dokkaDocsDirectory.dir("javadoc"))
  destinationDir = dokkaDocsDirectory.dir("javadoc").asFile
}

val dokkaVersion = tasks.register<Sync>("dokkaVersion") {
  group = "dokka"
  description = "Syncs content from docs/dokka to docs/versioned-dokka/${project.version}"
  from(dokkaHtmlCapture)
  into(dokkaDocsDirectory.dir("versioned-dokka/${project.version}"))
  destinationDir = dokkaDocsDirectory.dir("versioned-dokka/${project.version}").asFile
}

val dokkaCapture = tasks.register("dokkaCapture") {
  group = "dokka"
  description = "Runs `dokkaHtmlCapture` and `dokkaJavadocCapture`, funnelling results into docs/"
  dependsOn(dokkaHtmlCapture, dokkaJavadocCapture)
  finalizedBy(dokkaVersion)
}

/**
 * @see <a href="https://github.com/Kotlin/dokka/blob/v2.0.0/examples/gradle-v2/library-publishing-example/build.gradle.kts">Dokka Examples</a>
 */
val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
  group = "dokka"
  description = "A Javadoc JAR containing Dokka Javadoc"
  from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
  archiveClassifier = "javadoc"
}

val dokkaHtmlJar = tasks.register<Jar>("dokkaHtmlJar") {
  group = "dokka"
  description = "A HTML Documentation JAR containing Dokka HTML"
  from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
  archiveClassifier = "html-doc"
}

dependencies {
  dokkaPlugin(
    dependencyNotation = libs.findLibrary("dokka-plugin-versioning").get().get()
  )
  // dokkaPlugin("org.jetbrains.dokka:gfm-plugin")
}