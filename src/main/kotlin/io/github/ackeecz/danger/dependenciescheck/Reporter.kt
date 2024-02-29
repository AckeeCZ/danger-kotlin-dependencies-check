package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.DependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.update.OutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerableDependency
import systems.danger.kotlin.sdk.DangerContext

internal class Reporter(private val dangerContext: DangerContext) {

    fun reportOutdated(outdatedDependencies: List<OutdatedDependency>) {
        outdatedDependencies.forEach {
            dangerContext.warn(createOutdatedDependencyMessage(it))
        }
    }

    /**
     * Reports vulnerable dependencies to Danger.
     *
     * If the dependency has an available update, it is reported as a failure.
     *
     * If the dependency has no available update, it is reported as a warning.
     *
     * If the dependency version status is not known (dependency is not included in [dependenciesUpdateReport]), it
     * is reported as a warning with the information that it is a transitive dependency, because all direct dependencies
     * are reported in [dependenciesUpdateReport] either as outdated or up-to-date ones.
     */
    fun reportVulnerable(
        vulnerableDependencies: List<VulnerableDependency>,
        dependenciesUpdateReport: DependenciesUpdateReport,
    ) {
        vulnerableDependencies.forEach { dependency ->
            val foundOutdated = dependenciesUpdateReport.outdatedDependencies.firstOrNull { it.name == dependency.name }
            val foundUpToDate = dependenciesUpdateReport.upToDateDependencies.firstOrNull { it.name == dependency.name }
            when {
                foundOutdated == null && foundUpToDate == null -> {
                    dangerContext.warn(createVulnerableTransitiveDependencyMessage(dependency))
                }
                foundOutdated == null -> {
                    dangerContext.warn(createVulnerableDependencyWithoutUpdateMessage(dependency))
                }
                else -> {
                    val message = createVulnerableDependencyWithUpdateMessage(
                        vulnerableDependency = dependency,
                        newestAvailableVersion = foundOutdated.newestAvailableVersion,
                    )
                    dangerContext.fail(message)
                }
            }
        }
    }

    companion object {

        fun createOutdatedDependencyMessage(dependency: OutdatedDependency): String {
            return with(dependency) {
                "Outdated dependency: ${name.fullyQualifiedNameWithVersion}. Newest available version: ${newestAvailableVersion.value}"
            }
        }

        fun createVulnerableTransitiveDependencyMessage(dependency: VulnerableDependency): String {
            val generalMessage = createGeneralVulnerabilityMessage(dependency)
            return "$generalMessage Update unknown because this is a transitive dependency."
        }

        private fun createGeneralVulnerabilityMessage(dependency: VulnerableDependency): String {
            val vulnerabilities = dependency.vulnerabilities.joinToString { it.value }
            return "Found vulnerabilities in dependency: ${dependency.name.fullyQualifiedNameWithVersion}, vulnerabilities: $vulnerabilities."
        }

        fun createVulnerableDependencyWithoutUpdateMessage(dependency: VulnerableDependency): String {
            val generalMessage = createGeneralVulnerabilityMessage(dependency)
            return "$generalMessage No update found."
        }

        fun createVulnerableDependencyWithUpdateMessage(
            vulnerableDependency: VulnerableDependency,
            newestAvailableVersion: Version,
        ): String {
            val generalMessage = createGeneralVulnerabilityMessage(vulnerableDependency)
            return "$generalMessage Please update to ${newestAvailableVersion.value}"
        }
    }
}
