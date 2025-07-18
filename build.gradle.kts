@file:[
    Suppress("UnstableApiUsage")
    OptIn(
        ExperimentalKotlinGradlePluginApi::class,
        InternalKotlinGradlePluginApi::class,
    )
]

import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.defaultConfigs
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokkaDocsDirectory
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokkaKDocsDirectory
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.versionedDokkaDocsDirectory
import org.jetbrains.dokka.gradle.engine.parameters.KotlinPlatform
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    alias(libs.plugins.gradle.plugin.publish)

    // Apply the `signing` plugin and configure appropriately
    // https://docs.gradle.org/current/userguide/signing_plugin.html
    signing

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Dokka
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc)
}

version = libs.versions.project.get()

java {
    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()
    jvmToolchain(
        libs.versions.jdk.map { it.toInt() }.get()
    )
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
        jvmTarget = JvmTarget.JVM_17
        verbose = true
        optIn.addAll(
            "kotlin.ExperimentalStdlibApi",
        )
    }
}

gradlePlugin {
    website = "https://fnc314.com/${rootProject.name}"
    vcsUrl = "https://github.com/fnc314/${rootProject.name}"
    // Define the plugin
    val projectCollectionsGradleSettingsPlugin by plugins.creating {
        id = "com.fnc314.gradle.plugins.settings.${rootProject.name}"
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

    dokkaSourceSets {
        main {
            sourceRoots = rootProject.layout.projectDirectory.files("src/main/kotlin")
        }
        configureEach {
            includes.from(
                rootProject.layout.projectDirectory.files(
                    "README.md", "dokka.md"
                )
            )
            displayName = name

            suppress = false
            suppressGeneratedFiles = false
            enableJdkDocumentationLink = true
            enableKotlinStdLibDocumentationLink = true
            enableAndroidDocumentationLink = false
            reportUndocumented = true

            documentedVisibilities = setOf(VisibilityModifier.Public)
            analysisPlatform = KotlinPlatform.JVM
            languageVersion = libs.versions.kotlin.map { it.substringBeforeLast(".") }
            apiVersion = libs.versions.kotlin.map { it.substringBeforeLast(".") }
            jdkVersion = libs.versions.jdk.map { it.toInt() }

            sourceLink {
                localDirectory = rootProject.layout.projectDirectory.dir("src")
                remoteUrl = uri("https://github.com/fnc314/${rootProject.name}/tree/main/src")
                remoteLineSuffix = "#L"
            }

            perPackageOptions.all {
                documentedVisibilities = setOf(VisibilityModifier.Public)
                skipDeprecated = true
            }

            externalDocumentationLinks.maybeCreate("gradle").apply {
                url = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc")
                packageListUrl = uri("https://docs.gradle.org/${gradle.gradleVersion}/javadoc/element-list")
            }

            logger.error("DKK -> ${sourceSetId.get()}")
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
            version = libs.versions.project
            renderVersionsNavigationOnAllPages = true

            olderVersionsDir = versionedDokkaDocsDirectory
        }
    }

    dokkaPublications {
        configureEach {
            moduleName = rootProject.name
            moduleVersion = libs.versions.project
            suppressInheritedMembers = true
            suppressObviousFunctions = true
            includes.from(
                rootProject.layout.projectDirectory.files(
                    "README.md", "dokka.md"
                )
            )
        }
        html {
            outputDirectory = dokkaDocsDirectory
        }
        javadoc {
            outputDirectory = dokkaKDocsDirectory
        }
    }
}

// See: https://github.com/Kotlin/dokka/blob/v2.0.0/examples/gradle-v2/library-publishing-example/build.gradle.kts
// val dokkaJavadocJar by tasks.registering(Jar::class) {
//     group = "dokka"
//     description = "A Javadoc JAR containing Dokka Javadoc"
//     from(tasks.dokkaGeneratePublicationJavadoc.map { it.outputDirectory })
//     archiveClassifier = "javadoc"
// }

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
    from(dokkaDocsDirectory) {
        exclude("older")
    }
    into(libs.versions.project.map { versionedDokkaDocsDirectory.dir(it) })
    dependsOn(tasks.dokkaGenerate)
}

val dokkaOutput by tasks.registering(Zip::class) {
    group = "dokka"
    description = "Contains a reference to ${dokkaDocsDirectory.asFile} for publication"
    dependsOn(tasks.dokkaGenerate)
    mustRunAfter(tasks.dokkaGenerate)
    from(dokkaDocsDirectory)
    destinationDirectory = rootProject.layout.buildDirectory.dir("docs")
    archiveClassifier = "html-doc"
}

// tasks.dokkaGenerate.configure { finalizedBy(dokkaVersion) }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fnc314/${rootProject.name}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(dokkaHtmlJar)
            defaultConfigs(project = project.rootProject)
        }
    }
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use KotlinTest test framework
            useKotlinTest(libs.versions.kotlin.get())
        }

        // Create a new test suite
        val functionalTest by registering(JvmTestSuite::class) {
            // Use KotlinTest test framework
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