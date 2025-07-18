package com.fnc314.gradle.plugins.settings.projectcollectionsgradlesettingsplugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Implements [ProjectCollectionsGradleSettingsExtension] to expose [registerProjectCollection] and
 *   [registerNestedProjectCollection] methods
 * @param objectFactory An instance of the [ObjectFactory]
 */
internal abstract class ProjectCollectionsGradleSettingsExtensionImpl @Inject constructor(
    private val objectFactory: ObjectFactory,
) : ProjectCollectionsGradleSettingsExtension() {
    override val projectCollections: MapProperty<String, Int> = objectFactory.mapProperty(
        String::class.java, Int::class.java
    )

    override val fileCheck: Property<FileCheck> =
        objectFactory.property<FileCheck>().convention(null as FileCheck)

    override fun registerProjectCollection(topLevelDir: String, depth: Int) {
        projectCollections.put(topLevelDir, depth)
    }

    override infix fun String.toDepthOf(depth: Int) {
        projectCollections.put(this, depth)
    }

    override fun registerNestedProjectCollection(topLevelDir: String, depth: Int) {
        projectCollections.put(topLevelDir, depth)
    }
}