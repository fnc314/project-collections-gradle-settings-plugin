@file:[
    Suppress("UnstableApiUsage")
    OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
]

import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokkaDocsDirectory
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.versionedDokkaDocsDirectory
import org.gradle.kotlin.dsl.support.listFilesOrdered
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    alias(libs.plugins.gradle.plugin.publish)

    // Apply the `signing` plugin and configure appropriately
    // https://docs.gradle.org/current/userguide/signing_plugin.html
    signing

    // Publishing to GitHub Packages
    `maven-publish`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Dokka
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

version = libs.versions.project.get()

kotlin {
    jvmToolchain(
        libs.versions.jdk.map { it.toInt() }.get()
    )
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
        jvmTarget = JvmTarget.JVM_17
        optIn.addAll(
            "kotlin.ExperimentalStdlibApi",
        )
    }
}

java {
    withSourcesJar()
}

gradlePlugin {
    website = "https://fnc314.com/project-collection-gradle-settings-plugin"
    vcsUrl = "https://github.com/fnc314/project-collections-gradle-settings-plugin"
    // Define the plugin
    val projectCollectionsGradleSettingsPlugin by plugins.creating {
        id = "com.fnc314.gradle.plugins.settings.project-collections-gradle-settings-plugin"
        implementationClass = "com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin.ProjectCollectionsGradleSettingsPlugin"
        tags = listOf("gradle settings", "settings plugin", "gradle settings plugin",)
        description = "A plugin for `org.gradle.api.initialization.Settings` to streamline calls to `org.gradle.api.initialization.Settings.include` for arbitrarily nested sub-projects"
        displayName = "Project Collections Gradle Settings Plugin"
    }
}

dokka {
    moduleName = rootProject.name
    moduleVersion = libs.versions.project
    basePublicationsDirectory = dokkaDocsDirectory
    dokkaEngineVersion = libs.versions.dokka
    dokkaPublications {
        configureEach {
            moduleVersion = libs.versions.project
            moduleName = rootProject.name
            suppressInheritedMembers = true
            suppressObviousFunctions = true
            includes.from(
                rootProject.layout.projectDirectory.file("dokka.md"),
                rootProject.layout.projectDirectory.file("README.md")
            )
        }
        html {
            outputDirectory = dokkaDocsDirectory
        }
        javadoc {
            outputDirectory = dokkaDocsDirectory.dir("javadoc")
        }
    }
    dokkaSourceSets {
        configureEach {
            includes.from(
                rootProject.layout.projectDirectory.file("dokka.md"),
                rootProject.layout.projectDirectory.file("README.md")

            )
            displayName = name
            documentedVisibilities = setOf(VisibilityModifier.Public)
            suppressGeneratedFiles = true
            enableJdkDocumentationLink = true
            enableKotlinStdLibDocumentationLink = true
            enableAndroidDocumentationLink = false
            reportUndocumented = true
            analysisPlatform = KotlinPlatform.JVM
            languageVersion = libs.versions.kotlin.map { it.substringBeforeLast(".") }
            apiVersion = libs.versions.kotlin.map { it.substringBeforeLast(".") }
            jdkVersion = libs.versions.jdk.map { it.toInt() }

            sourceLink {
                localDirectory = layout.projectDirectory.dir("src")
                remoteUrl = uri("https://github.com/fnc314/project-collections-gradle-settings-plugin/tree/main/src")
                remoteLineSuffix = "#L"
            }

            perPackageOptions.all {
                documentedVisibilities = setOf(VisibilityModifier.Public)
                skipDeprecated = true
                suppress = false
            }

            externalDocumentationLinks.maybeCreate("gradle").apply {
                url = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc")
                packageListUrl = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc/element-list")
            }
        }
        main {
            sourceRoots = rootProject.layout.projectDirectory.files("src/main")
        }
        test {
            sourceRoots = rootProject.layout.projectDirectory.files("src/main", "src/test", "src/functionalTest")
        }
        javaMain {
            sourceRoots = rootProject.layout.projectDirectory.files("src/main/java")
        }
        javaTest {
            sourceRoots = rootProject.layout.projectDirectory.files("src/main/java", "src/test/java", "src/functionalTest/java")
        }
    }
    pluginsConfiguration {
        html {
            homepageLink.value("http://www.fnc314.com/project-collections-gradle-settings-plugin/")
            footerMessage.value(
                provider {
                    buildString {
                        append("(C) <a href=\"https://fnc314.com\" target=\"_blank\">fnc314</a>")
                        append(" | ")
                        append("<a href=\"javadoc\" target=\"_blank\">javadoc</a>")
                    }
                }
            )
        }
        versioning {
            version = libs.versions.project
            renderVersionsNavigationOnAllPages = true
            olderVersions = files(
                versionedDokkaDocsDirectory.asFile.listFilesOrdered { versionedDokka ->
                    versionedDokka.isDirectory and versionedDokka.name.split(".").run {
                        (size == 3) and all { it.toIntOrNull() != null }
                    }
                }
            )
            olderVersionsDir = versionedDokkaDocsDirectory
        }
    }
}

// See: https://github.com/Kotlin/dokka/blob/v2.0.0/examples/gradle-v2/library-publishing-example/build.gradle.kts
val dokkaJavadocJar by tasks.registering(Jar::class) {
    group = "dokka"
    description = "A Javadoc JAR containing Dokka Javadoc"
    from(tasks.dokkaGeneratePublicationJavadoc.map { it.outputDirectory })
    archiveClassifier = "javadoc"
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
    group = "dokka"
    description = "A HTML Documentation JAR containing Dokka HTML"
    from(tasks.dokkaGeneratePublicationHtml.map { it.outputDirectory })
    archiveClassifier = "html-doc"
}

val dokkaClean by tasks.registering(Delete::class) {
    group = "dokka"
    description = "Cleans the ${dokkaDocsDirectory.asFile} directory"
    delete(dokkaDocsDirectory.asFileTree.files)
}

val dokkaVersion by tasks.registering(Sync::class) {
    group = "dokka"
    description = "Syncs the output of ${dokkaDocsDirectory.asFile} with versioned-docs/${libs.versions.project.get()}"
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    from(dokkaDocsDirectory)
    into(libs.versions.project.map { versionedDokkaDocsDirectory.dir(it) })
    dependsOn(tasks.dokkaGenerate)
}

tasks.dokkaGenerate.configure { finalizedBy(dokkaVersion) }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fnc314/project-collections-gradle-settings-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(dokkaJavadocJar)
            artifact(dokkaHtmlJar)

            pom {
                name = "Project Collections Gradle Settings Plugin"
                version = "${libs.versions.project.get()}"
                description = "A Gradle Settings Plugin to streamline `include` calls to arbitrarily nested sub-directories"
                url = "https://fnc314.com/project-collection-gradle-settings-plugin"
                developers {
                    developer {
                        id = "fnc314"
                        name = "Franco N. Colaizzi"
                        email = "fnc314@fnc314.com"
                    }
                }
                scm {
                    url = "https://github.com/fnc314/project-collection-gradle-settings-plugin"
                }
                distributionManagement {
                    downloadUrl = "https://github.com/fnc314/project-collections-gradle-settings-plugin/packages"
                }
            }
        }
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(libs.versions.kotlin.get())
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest(libs.versions.kotlin.get())

            dependencies {
                // functionalTest test suite depends on the production code in tests
                implementation(project())
            }

            targets {
                all {
                    // This test suite should run after the built-in test suite has run its tests
                    testTask.configure { shouldRunAfter(test) }
                }
            }
        }
    }
}

tasks {
    named<Task>("check") {
        // Include functionalTest as part of the check lifecycle
        dependsOn(testing.suites.named("functionalTest"))
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

dependencies {
    compileOnly(gradleKotlinDsl())
    compileOnly(libs.kotlin)
    dokkaHtmlPlugin(libs.dokka.plugin.versioning)
}