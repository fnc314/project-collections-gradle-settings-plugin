package com.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin

import org.gradle.api.Project
import org.gradle.api.file.Directory

/** A [Directory] pointing to [Project.getProjectDir]/docs */
val Project.dokkaDocsDirectory: Directory
    get() = rootProject.layout.projectDirectory.dir("docs")

/** A [Directory] pointing to [Project.getProjectDir]/versioned-docs */
val Project.versionedDokkaDocsDirectory: Directory
    get() = rootProject.layout.projectDirectory.dir("versioned-docs")