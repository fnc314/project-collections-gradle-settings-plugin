@file:[
    Suppress("UnstableApiUsage")
    OptIn(
        ExperimentalKotlinGradlePluginApi::class,
        InternalKotlinGradlePluginApi::class,
    )
]

import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.defaultConfigs
import com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.kotlinVersion
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Dokka
    alias(libs.plugins.dokka.config)

    // Publishing (and Signing)
    alias(libs.plugins.publishing)
}

version = libs.versions.project.get()

kotlin {
    explicitApi()
    jvmToolchain(
        libs.versions.jdk.map { it.toInt() }.get()
    )
    compilerOptions {
        jvmTarget = libs.versions.jdk.map { JvmTarget.fromTarget(it) }
        apiVersion = KotlinVersion.fromVersion(kotlinVersion)
        languageVersion = KotlinVersion.fromVersion(kotlinVersion)
        verbose = true
        optIn.addAll(
            "kotlin.ExperimentalStdlibApi",
        )
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifact(tasks.dokkaHtmlJar)
            artifact(tasks.dokkaJavadocJar)
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