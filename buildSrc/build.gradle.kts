import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(
        libs.versions.jdk.map { it.toInt() }.get()
    )
    compilerOptions {
        jvmTarget = libs.versions.jdk.map { JvmTarget.fromTarget(it) }
        apiVersion = libs.versions.kotlin.map {
            KotlinVersion.fromVersion(it.substringBeforeLast("."))
        }
        languageVersion = libs.versions.kotlin.map {
            KotlinVersion.fromVersion(it.substringBeforeLast("."))
        }
    }
}

dependencies {
    compileOnly(gradleKotlinDsl())
    runtimeOnly(libs.kotlin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.publish.gradle.plugin)
    implementation(libs.bundles.dokka)
}