package io.github.ackeecz.danger.dependenciescheck.config

import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.Vulnerability

internal fun createConfig(
    vulnerabilitiesConfig: Config.Vulnerabilities = createVulnerabilitiesConfig(),
    outdatedDependenciesConfig: Config.OutdatedDependencies = createOutdatedDependenciesConfig(),
): Config {
    return Config(
        vulnerabilitiesConfig = vulnerabilitiesConfig,
        outdatedDependenciesConfig = outdatedDependenciesConfig,
    )
}

internal fun createVulnerabilitiesConfig(
    suppressions: List<VulnerabilitySuppression> = emptyList(),
): Config.Vulnerabilities {
    return Config.Vulnerabilities(
        suppressions = suppressions,
    )
}

internal fun createVulnerabilitySuppression(
    fullyQualifiedName: String = "group-id:artifact-id",
    vulnerabilities: List<String> = listOf("CVE-2018-1000850"),
): VulnerabilitySuppression {
    return VulnerabilitySuppression(
        fullyQualifiedName = fullyQualifiedName,
        vulnerabilities = vulnerabilities.map { Vulnerability(it) },
    )
}

internal fun createOutdatedDependenciesConfig(
    suppressions: List<OutdatedDependencySuppression> = emptyList(),
): Config.OutdatedDependencies {
    return Config.OutdatedDependencies(
        suppressions = suppressions,
    )
}

internal fun createOutdatedDependencySuppression(
    fullyQualifiedNameWithVersion: String,
): OutdatedDependencySuppression {
    return OutdatedDependencySuppression(fullyQualifiedNameWithVersion = fullyQualifiedNameWithVersion)
}
