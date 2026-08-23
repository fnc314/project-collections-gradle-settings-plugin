package dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokka


/**
 * Precompiled [dokka-config.gradle.kts][dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokka.Dokka_config_gradle] script plugin.
 *
 * @see dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokka.Dokka_config_gradle
 */
public
class DokkaConfigPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("dev.fnc314.gradle.settings.plugin.projectcollectionsgradlesettingsplugin.dokka.Dokka_config_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
