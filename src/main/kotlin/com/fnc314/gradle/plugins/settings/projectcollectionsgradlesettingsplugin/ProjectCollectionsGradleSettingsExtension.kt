package com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import java.io.File

/** A Convenience for [ProjectCollectionsGradleSettingsExtension.fileCheck] */
typealias FileCheck = java.util.function.Function<File, Boolean>?

/**
 * Attached to a [org.gradle.api.initialization.Settings] object to configure the
 *   [ProjectCollectionsGradleSettingsPlugin]
 */
abstract class ProjectCollectionsGradleSettingsExtension {
    /** @hide */
    internal companion object Companion {
        /** The name for the actual extension found in [org.gradle.api.initialization.Settings.getExtensions] */
        const val EXTENSION_NAME: String = "projectCollections"
    }

    /**
     * A [Map] of the top-level directory name (as a [String]) as well as the [Int] representing how
     *   nested projects are within the top-level directory.  Unexposed to consumers
     */
    internal abstract val projectCollections: MapProperty<String, Int>

    /**
     * An optional [java.util.function.Function] operating on a [File] and returning a [Boolean]
     *   acting as eligibility criteria for [File.isDirectory] instances to adhere to before passing to
     *   [org.gradle.api.initialization.Settings.include(java.lang.String...)]
     * ```kotlin
     * projectCollections {
     *     fileCheck.set { file -> false }
     * }
     * ```
     */
    abstract val fileCheck: Property<FileCheck>

    /**
     * Register a collection of 1-level-deep projects
     * ```kotlin
     * projectCollections {
     *     registerProjectCollection(topLevelDir = "some-dir")
     *     // implies "some-dir/first", "some-dir/second", etc...
     * }
     * ```
     * @param topLevelDir The collection name/top-level directory
     * @see registerProjectCollection
     */
    fun registerProjectCollection(topLevelDir: String) {
        registerProjectCollection(topLevelDir = topLevelDir, depth = 1)
    }

    /**
     * Register a collection of nested projects
     * ```kotlin
     * projectCollections {
     *     registerProjectCollection(topLevelDir = "some-dir", depth = 3)
     *     // implies "some-dir/first/second/desired", "some-dir/other/layer/target", etc...
     * }
     * ```
     * @param topLevelDir The collection name/top-level directory
     * @param depth The depth within [topLevelDir] which must be traversed to find a desired project
     */
    abstract fun registerProjectCollection(topLevelDir: String, depth: Int)

    /**
     * A friendly-syntax approach to including collections of projects
     * ```kotlin
     * projectCollections {
     *     "apps" toDepthOf 1
     *     "components" toDepthOf 1
     *     "design-system" toDepthOf 1
     *     "features" toDepthOf 2
     *     // means "features/first/project-a", "features/first/project-b", "features/second/project-a", etc...
     * }
     * ```
     * @receiver A [String] interpreted as the top-level directory name
     * @param depth An [Int] indicating how deep into the top-level directory members we are required to traverse
     */
    abstract infix fun String.toDepthOf(depth: Int)

    /**
     * Register a collection of nested projects
     * ```kotlin
     * projectCollections {
     *     registerNestedProjectCollection(topLevelDir = "some-dir", depth = 3)
     *     // implies "some-dir/first/second/desired", "some-dir/other/layer/target", etc...
     * }
     * ```
     * @param topLevelDir The collection name/top-level directory
     * @param depth The depth within [topLevelDir] which must be traversed to find a desired project
     * @see registerProjectCollection
     */
    abstract fun registerNestedProjectCollection(topLevelDir: String, depth: Int)
}