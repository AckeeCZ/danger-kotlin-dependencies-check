package io.github.ackeecz.danger.dependenciescheck.config

import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.Vulnerability

public data class Config(
    val vulnerabilitiesConfig: Vulnerabilities = Vulnerabilities(),
    val outdatedDependenciesConfig: OutdatedDependencies = OutdatedDependencies(),
) {

    /**
     * Config for vulnerabilities feature of the plugin.
     *
     * [reportFileName] - configures name of the file with reported vulnerabilities that the plugin should search for
     * [suppressions] - list of all suppressions of vulnerable dependencies. This can be used to suppress unwanted
     * Danger vulnerability reports (e.g. false positive reports or unfixable vulnerabilities).
     */
    public data class Vulnerabilities(
        val reportFileName: String = "dependency-check-report.xml",
        val suppressions: List<VulnerabilitySuppression> = emptyList(),
    )

    /**
     * Config for outdated dependencies feature of the plugin.
     *
     * [reportFileName] - configures name of the file with reported outdated dependencies that the plugin should search for
     * [suppressions] - list of all suppressions of outdated dependencies. This can be used to suppress unwanted
     * Danger outdated dependencies reports (e.g. not possible to update).
     */
    public data class OutdatedDependencies(
        val reportFileName: String = "dependency-updates-report.xml",
        val suppressions: List<OutdatedDependencySuppression> = emptyList(),
    )
}

/**
 * Suppresses Danger reports of vulnerabilities identified by CVE IDs for the given dependency identified by a groupId and artifactId
 */
public data class VulnerabilitySuppression internal constructor(
    val fullyQualifiedName: String,
    internal val vulnerabilities: List<Vulnerability>,
) {

    init {
        require(vulnerabilities.isNotEmpty()) { "Vulnerabilities must not be empty" }
    }

    public constructor(
        groupId: String,
        artifactId: String,
        vulnerabilities: List<String>,
    ) : this(
        fullyQualifiedName = "$groupId:$artifactId",
        vulnerabilities = vulnerabilities.map { Vulnerability(it) },
    )
}

/**
 * Suppresses a Danger report of an outdated dependency identified by its fully qualified name with a version
 * (e.g. com.squareup.retrofit2:retrofit:2.8.2)
 */
public data class OutdatedDependencySuppression(val fullyQualifiedNameWithVersion: String)
