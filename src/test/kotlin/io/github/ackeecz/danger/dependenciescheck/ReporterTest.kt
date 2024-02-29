package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.createDependencyName
import io.github.ackeecz.danger.dependenciescheck.model.update.createDependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.update.createOutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.update.createUpToDateDependency
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.Vulnerability
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.createVulnerableDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import systems.danger.kotlin.sdk.DangerContext

private lateinit var dangerContext: DangerContext
private lateinit var underTest: Reporter

internal class ReporterTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        underTest = Reporter(dangerContext)
    }

    testOutdatedDependencies()
    testVulnerabilities()
})

private fun FunSpec.testOutdatedDependencies() = context("outdated dependencies") {
    test("report outdated dependencies") {
        val dependency1 = createOutdatedDependency(
            name = createDependencyName(groupId = GroupId("group-id-1")),
            newestAvailableVersion = Version("2.0.0"),
        )
        val dependency2 = createOutdatedDependency(
            name = createDependencyName(groupId = GroupId("group-id-2")),
            newestAvailableVersion = Version("3.0.0"),
        )

        underTest.reportOutdated(listOf(dependency1, dependency2))

        val actualWarnings = dangerContext.warnings.map { it.message }
        actualWarnings.forEach(::println)
        actualWarnings shouldHaveSize 2
        actualWarnings shouldContain Reporter.createOutdatedDependencyMessage(dependency1)
        actualWarnings shouldContain Reporter.createOutdatedDependencyMessage(dependency2)
    }
}

private fun FunSpec.testVulnerabilities() = context("vulnerabilities") {
    test("report vulnerable transitive dependencies") {
        val vulnerableDependency1 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-1")),
            vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2020-1000850")),
        )
        val vulnerableDependency2 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-2")),
            vulnerabilities = listOf(Vulnerability("CVE-2022-1000900")),
        )
        val dependenciesUpdateReport = createDependenciesUpdateReport(
            outdatedDependencies = emptyList(),
            upToDateDependencies = emptyList(),
        )

        underTest.reportVulnerable(listOf(vulnerableDependency1, vulnerableDependency2), dependenciesUpdateReport)

        val actualWarnings = dangerContext.warnings.map { it.message }
        actualWarnings.forEach(::println)
        actualWarnings shouldHaveSize 2
        actualWarnings shouldContain Reporter.createVulnerableTransitiveDependencyMessage(vulnerableDependency1)
        actualWarnings shouldContain Reporter.createVulnerableTransitiveDependencyMessage(vulnerableDependency2)
    }

    test("report vulnerable dependencies without available updates") {
        val vulnerableDependency1 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-1")),
            vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2020-1000850")),
        )
        val vulnerableDependency2 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-2")),
            vulnerabilities = listOf(Vulnerability("CVE-2022-1000900")),
        )
        val dependenciesUpdateReport = createDependenciesUpdateReport(
            outdatedDependencies = emptyList(),
            upToDateDependencies = listOf(
                createUpToDateDependency(name = vulnerableDependency1.name),
                createUpToDateDependency(name = vulnerableDependency2.name),
            ),
        )

        underTest.reportVulnerable(listOf(vulnerableDependency1, vulnerableDependency2), dependenciesUpdateReport)

        val actualWarnings = dangerContext.warnings.map { it.message }
        actualWarnings.forEach(::println)
        actualWarnings shouldHaveSize 2
        actualWarnings shouldContain Reporter.createVulnerableDependencyWithoutUpdateMessage(vulnerableDependency1)
        actualWarnings shouldContain Reporter.createVulnerableDependencyWithoutUpdateMessage(vulnerableDependency2)
    }

    test("report vulnerable dependencies with available updates") {
        val vulnerableDependency1 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-1"), version = Version("1.0.0")),
            vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2020-1000850")),
        )
        val vulnerableDependency2 = createVulnerableDependency(
            name = createDependencyName(groupId = GroupId("group-id-2"), version = Version("1.0.0")),
            vulnerabilities = listOf(Vulnerability("CVE-2022-1000900")),
        )
        val newestAvailableVersion = Version("2.0.0")
        val dependenciesUpdateReport = createDependenciesUpdateReport(
            outdatedDependencies = listOf(
                createOutdatedDependency(name = vulnerableDependency1.name, newestAvailableVersion = newestAvailableVersion),
                createOutdatedDependency(name = vulnerableDependency2.name, newestAvailableVersion = newestAvailableVersion),
            ),
            upToDateDependencies = emptyList(),
        )

        underTest.reportVulnerable(listOf(vulnerableDependency1, vulnerableDependency2), dependenciesUpdateReport)

        val actualFails = dangerContext.fails.map { it.message }
        actualFails.forEach(::println)
        actualFails shouldHaveSize 2
        actualFails shouldContain Reporter.createVulnerableDependencyWithUpdateMessage(
            vulnerableDependency = vulnerableDependency1,
            newestAvailableVersion = newestAvailableVersion,
        )
        actualFails shouldContain Reporter.createVulnerableDependencyWithUpdateMessage(
            vulnerableDependency = vulnerableDependency2,
            newestAvailableVersion = newestAvailableVersion,
        )
    }
}
