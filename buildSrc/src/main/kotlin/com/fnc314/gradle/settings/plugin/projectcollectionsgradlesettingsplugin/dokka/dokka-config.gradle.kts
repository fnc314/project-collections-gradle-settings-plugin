package com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokka

import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokkaDocsDirectory
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokkaDocsIntermediateDirectory
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.kotlinVersion
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.libs
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.jetbrains.dokka-javadoc")
}

val projectVersion: String =
    libs.findVersion("project").map { it.requiredVersion }.orElse(rootProject.version.toString())

dokka {
    moduleName = rootProject.name
    moduleVersion = projectVersion
    basePublicationsDirectory = dokkaDocsIntermediateDirectory
    modulePath = path
    sourceSetScopeDefault = path
    dokkaSourceSets {
        configureEach {
            sourceRoots = rootProject.layout.projectDirectory.files("src/${this@configureEach.name}/kotlin")
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
            languageVersion = kotlinVersion
            apiVersion = kotlinVersion
            jdkVersion = libs.findVersion("jdk").map { it.requiredVersion.toInt() }.orElse(17)

            sourceLink {
                localDirectory = rootProject.layout.projectDirectory.dir("src")
                remoteUrl = uri("https://github.com/fnc314/${rootProject.name}/tree/main/src")
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
            homepageLink.value("http://www.fnc314.com/${rootProject.name}/")
            footerMessage.value(
                provider {
                    buildString {
                        append("(C) <a href=\"https://fnc314.com\" target=\"_blank\">fnc314</a>")
                        append(" | ")
                        append("<a href=\"${rootProject.name}\" target=\"_blank\">javadoc</a>")
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
            moduleName = rootProject.name
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

val dokkaHtmlCapture by tasks.registering(Sync::class) {
    group = "dokka"
    description = "Syncs content from build/dokka into docs/dokka"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    into(dokkaDocsDirectory.dir("dokka"))
    destinationDir = dokkaDocsDirectory.dir("dokka").asFile
}

val dokkaJavadocCapture by tasks.registering(Sync::class) {
    group = "dokka"
    description = "Syncs content from build/dokka-javadoc into docs/javadoc}"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    into(dokkaDocsDirectory.dir("javadoc"))
    destinationDir = dokkaDocsDirectory.dir("javadoc").asFile
}

val dokkaVersion by tasks.registering(Sync::class) {
    group = "dokka"
    description = "Syncs content from docs/dokka to docs/versioned-dokka/${rootProject.version.toString()}"
    from(dokkaHtmlCapture)
    into(dokkaDocsDirectory.dir("versioned-dokka/${rootProject.version.toString()}"))
    destinationDir = dokkaDocsDirectory.dir("versioned-dokka/${rootProject.version.toString()}").asFile
}

val dokkaCapture by tasks.registering {
    group = "dokka"
    description = "Runs `dokkaHtmlCapture` and `dokkaJavadocCapture`, funnelling results into docs/"
    dependsOn(dokkaHtmlCapture, dokkaJavadocCapture)
    finalizedBy(dokkaVersion)
}

// See: https://github.com/Kotlin/dokka/blob/v2.0.0/examples/gradle-v2/library-publishing-example/build.gradle.kts
val dokkaJavadocJar by tasks.registering(Jar::class) {
    group = "dokka"
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier = "javadoc"
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
    group = "dokka"
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier = "html-doc"
}

dependencies {
    dokkaPlugin("org.jetbrains.dokka:versioning-plugin")
}