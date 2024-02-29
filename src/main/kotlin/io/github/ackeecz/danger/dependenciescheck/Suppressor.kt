package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.config.OutdatedDependencySuppression
import io.github.ackeecz.danger.dependenciescheck.config.VulnerabilitySuppression
import io.github.ackeecz.danger.dependenciescheck.model.FullyQualifiedName
import io.github.ackeecz.danger.dependenciescheck.model.update.DependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerabilitiesReport
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.Vulnerability
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerableDependency
import systems.danger.kotlin.sdk.DangerContext

internal class Suppressor(private val dangerContext: DangerContext) {

    fun suppressVulnerableDependencies(
        report: VulnerabilitiesReport,
        suppressions: List<VulnerabilitySuppression>,
    ): VulnerabilitiesReport {
        return VulnerabilitiesSuppressor(report, suppressions).suppress()
    }

    private inner class VulnerabilitiesSuppressor(
        private val report: VulnerabilitiesReport,
        private val suppressions: List<VulnerabilitySuppression>,
    ) {

        private val allUnusedSuppressions = mutableMapOf<FullyQualifiedName, List<Vulnerability>>()

        fun suppress(): VulnerabilitiesReport {
            return suppressVulnerabilities().also { reportUnusedSuppressions() }
        }

        private fun suppressVulnerabilities(): VulnerabilitiesReport {
            val filteredDependencies = report.dependencies.mapNotNull { dependency ->
                val foundSuppression = suppressions.find { it.fullyQualifiedName == dependency.name.fullyQualifiedName.value }
                if (foundSuppression == null) {
                    dependency
                } else {
                    suppressVulnerabilities(dependency, foundSuppression)
                }
            }
            return report.copy(dependencies = filteredDependencies)
        }

        private fun suppressVulnerabilities(
            dependency: VulnerableDependency,
            suppression: VulnerabilitySuppression,
        ): VulnerableDependency? {
            val filteredVulnerabilities = dependency.vulnerabilities - suppression.vulnerabilities.toSet()
            val unusedSuppressions = suppression.vulnerabilities - dependency.vulnerabilities.toSet()
            if (unusedSuppressions.isNotEmpty()) {
                allUnusedSuppressions[dependency.name.fullyQualifiedName] = unusedSuppressions
            }
            return if (filteredVulnerabilities.isEmpty()) {
                null
            } else {
                dependency.copy(vulnerabilities = filteredVulnerabilities)
            }
        }

        private fun reportUnusedSuppressions() {
            allUnusedSuppressions.forEach { nameToVulnerabilities ->
                nameToVulnerabilities.value.forEach { vulnerability ->
                    val message = createUnusedVulnerabilitySuppressionMessage(
                        fullyQualifiedName = nameToVulnerabilities.key.value,
                        vulnerability = vulnerability.value,
                    )
                    dangerContext.warn(message)
                }
            }
        }
    }

    fun suppressOutdatedDependencies(
        report: DependenciesUpdateReport,
        suppressions: List<OutdatedDependencySuppression>,
    ): DependenciesUpdateReport {
        return OutdatedDependenciesSuppressor(report, suppressions).suppress()
    }

    private inner class OutdatedDependenciesSuppressor(
        private val report: DependenciesUpdateReport,
        private val suppressions: List<OutdatedDependencySuppression>,
    ) {

        private val allUnusedSuppressions = suppressions.toMutableList()

        fun suppress(): DependenciesUpdateReport {
            return suppressVulnerabilities().also { reportUnusedSuppressions() }
        }

        private fun suppressVulnerabilities(): DependenciesUpdateReport {
            val filteredDependencies = report.outdatedDependencies.filter { dependency ->
                val foundSuppression = suppressions.find {
                    it.fullyQualifiedNameWithVersion == dependency.name.fullyQualifiedNameWithVersion
                }?.also {
                    allUnusedSuppressions.remove(it)
                }
                foundSuppression == null
            }
            return report.copy(outdatedDependencies = filteredDependencies)
        }

        private fun reportUnusedSuppressions() {
            allUnusedSuppressions.forEach { suppression ->
                val message = createUnusedOutdatedDependencySuppressionMessage(
                    fullyQualifiedNameWithVersion = suppression.fullyQualifiedNameWithVersion,
                )
                dangerContext.warn(message)
            }
        }
    }

    companion object {

        fun createUnusedVulnerabilitySuppressionMessage(
            fullyQualifiedName: String,
            vulnerability: String,
        ): String {
            return "Unused vulnerability suppression $vulnerability for dependency $fullyQualifiedName"
        }

        fun createUnusedOutdatedDependencySuppressionMessage(fullyQualifiedNameWithVersion: String): String {
            return "Unused outdated dependency suppression $fullyQualifiedNameWithVersion"
        }
    }
}
