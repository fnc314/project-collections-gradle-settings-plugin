package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.projectplugins.dokka

import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.dokkaDocsDirectory
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.dokkaDocsIntermediateDirectory
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.kotlinVersion
import dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.buildlogic.projectextensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.dokka.gradle.engine.plugins.DokkaHtmlPluginParameters
import org.jetbrains.dokka.gradle.engine.plugins.DokkaVersioningPluginParameters
import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask
import java.net.URI
import javax.inject.Inject
import kotlin.jvm.optionals.getOrDefault

abstract class DokkaConfigPluginNew @Inject constructor() : Plugin<Project> {
  override fun apply(target: Project) {
    target.pluginManager.apply("org.jetbrains.kotlin.jvm")
    target.pluginManager.apply("org.jetbrains.dokka")
    target.pluginManager.apply("org.jetbrains.dokka-javadoc")

    val projectVersion: String =
      target.libs.findVersion("project").map { it.requiredVersion }.getOrDefault(target.version.toString())

    target.configure<DokkaExtension> {
      moduleName.set(target.name)
      moduleVersion.set(projectVersion)
      basePublicationsDirectory.set(target.dokkaDocsIntermediateDirectory)
      modulePath.set(target.path)
      sourceSetScopeDefault.set(target.path)

      dokkaSourceSets.apply {
        configureEach {
          sourceRoots.setFrom(target.layout.projectDirectory.files("src/${this@configureEach.name}/kotlin"))
          includes.from(
            target.layout.settingsDirectory.files(
              "README.md", "dokka.md"
            )
          )

          suppress.set(this@configureEach.name != "main")
          suppressGeneratedFiles.set(false)
          enableJdkDocumentationLink.set(true)
          enableKotlinStdLibDocumentationLink.set(true)
          enableAndroidDocumentationLink.set(false)
          reportUndocumented.set(true)

          documentedVisibilities.set(setOf(VisibilityModifier.Public))
          analysisPlatform.set(KotlinPlatform.JVM)
          languageVersion.set(target.kotlinVersion)
          apiVersion.set(target.kotlinVersion)
          jdkVersion.set(
            target.libs.findVersion("jdk")
              .map { it.requiredVersion.toInt() }
              .get()
          )

          sourceLink {
            localDirectory.set(target.layout.projectDirectory.dir("src"))
            remoteUrl.set(URI.create("https://github.com/fnc314/${target.name}/tree/main/${target.projectDir.name}/src"))
            remoteLineSuffix.set("#L")
          }

          perPackageOptions.all {
            documentedVisibilities.set(setOf(VisibilityModifier.Public))
            skipDeprecated.set(true)
            reportUndocumented.set(true)
          }

          externalDocumentationLinks.maybeCreate("gradle").apply {
            url.set(URI.create("https://docs.gradle.org/${target.gradle.gradleVersion}/javadoc"))
            packageListUrl.set(URI.create("https://docs.gradle.org/${target.gradle.gradleVersion}/javadoc/element-list"))
          }

          target.logger.lifecycle("DSS -> ${sourceSetId.get()}")
        }
      }

      pluginsConfiguration.apply {
        register("html", DokkaHtmlPluginParameters::class.java) {
          homepageLink.set("https://www.fnc314.dev/${target.name}/")
          footerMessage.set(
            target.provider {
              buildString {
                append("(C) <a href=\"https://www.fnc314.dev\" target=\"_blank\">fnc314</a>")
                append(" | ")
                append("<a href=\"https://www.fnc314.dev/${target.name}/dokka\" target=\"_blank\">dokka</a>")
                append(" | ")
                append("<a href=\"https://www.fnc314.dev/${target.name}/javadoc\" target=\"_blank\">javadoc</a>")
                append(" | ")
                append(target.version)
              }
            }
          )
        }
        register("versioning", DokkaVersioningPluginParameters::class.java) {
          version.set(projectVersion)
          renderVersionsNavigationOnAllPages.set(true)
          olderVersionsDir.set(target.dokkaDocsDirectory.dir("versioned-dokka"))
        }
      }

      dokkaPublications.apply {
        configureEach {
          moduleName.set(target.name)
          moduleVersion.set(projectVersion)
          suppressInheritedMembers.set(true)
          suppressObviousFunctions.set(true)
          includes.from(
            target.layout.settingsDirectory.files(
              "README.md", "dokka.md"
            )
          )
        }
      }
    }

    // Add dokka versioning plugin dependency
    target.dependencies.add(
      "dokkaPlugin",
      target.libs.findLibrary("dokka-plugin-versioning").get().get()
    )

    val dokkaHtmlCapture = target.tasks.register<Sync>("dokkaHtmlCapture") {
      group = "dokka"
      description = "Syncs content from build/dokka into docs/dokka"
      from(target.tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml").flatMap { it.outputDirectory })
      into(target.dokkaDocsDirectory.dir("dokka"))
      destinationDir = target.dokkaDocsDirectory.dir("dokka").asFile
    }

    val dokkaJavadocCapture = target.tasks.register<Sync>("dokkaJavadocCapture") {
      group = "dokka"
      description = "Syncs content from build/dokka-javadoc into docs/javadoc"
      from(target.tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationJavadoc").flatMap { it.outputDirectory })
      into(target.dokkaDocsDirectory.dir("javadoc"))
      destinationDir = target.dokkaDocsDirectory.dir("javadoc").asFile
    }

    val dokkaVersion = target.tasks.register<Sync>("dokkaVersion") {
      group = "dokka"
      description = "Syncs content from docs/dokka to docs/versioned-dokka/${target.version}"
      from(dokkaHtmlCapture)
      into(target.dokkaDocsDirectory.dir("versioned-dokka/${target.version}"))
      destinationDir = target.dokkaDocsDirectory.dir("versioned-dokka/${target.version}").asFile
    }

    target.tasks.register("dokkaCapture") {
      group = "dokka"
      description = "Runs `dokkaHtmlCapture` and `dokkaJavadocCapture`, funnelling results into docs/"
      dependsOn(dokkaHtmlCapture, dokkaJavadocCapture)
      finalizedBy(dokkaVersion)
    }

    target.tasks.register<Jar>("dokkaJavadocJar") {
      group = "dokka"
      description = "A Javadoc JAR containing Dokka Javadoc"
      from(target.tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationJavadoc").flatMap { it.outputDirectory })
      archiveClassifier.set("javadoc")
    }

    target.tasks.register<Jar>("dokkaHtmlJar") {
      group = "dokka"
      description = "A HTML Documentation JAR containing Dokka HTML"
      from(target.tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml").flatMap { it.outputDirectory })
      archiveClassifier.set("html-doc")
    }
  }
}
