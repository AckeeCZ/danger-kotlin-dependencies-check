package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.config.OutdatedDependencySuppression
import io.github.ackeecz.danger.dependenciescheck.config.VulnerabilitySuppression
import io.github.ackeecz.danger.dependenciescheck.config.createOutdatedDependencySuppression
import io.github.ackeecz.danger.dependenciescheck.config.createVulnerabilitySuppression
import io.github.ackeecz.danger.dependenciescheck.model.ArtifactId
import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.DependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.update.OutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.update.UpToDateDependency
import io.github.ackeecz.danger.dependenciescheck.model.update.createOutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.update.createUpToDateDependency
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerabilitiesReport
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.Vulnerability
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerableDependency
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.createVulnerableDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import systems.danger.kotlin.sdk.DangerContext

private lateinit var dangerContext: DangerContext
private lateinit var underTest: Suppressor

internal class SuppressorTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        underTest = Suppressor(dangerContext)
    }

    testVulnerabilities()
    testOutdatedDependencies()
})

private fun FunSpec.testVulnerabilities() = context("vulnerabilities") {
    test("suppress all vulnerabilities from multiple dependencies") {
        val suppressions = listOf(
            createVulnerabilitySuppression(
                fullyQualifiedName = "com.squareup.retrofit2:retrofit",
                vulnerabilities = listOf("CVE-2018-1000844", "CVE-2018-1000850"),
            ),
            createVulnerabilitySuppression(
                fullyQualifiedName = "androidx.datastore:datastore-preferences-core",
                vulnerabilities = listOf("CVE-2022-3171", "CVE-2022-3510"),
            ),
        )
        val report = VulnerabilitiesReport(
            dependencies = listOf(
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("com.squareup.retrofit2"),
                        artifactId = ArtifactId("retrofit"),
                        version = Version("2.4.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2018-1000850")),
                ),
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("androidx.datastore"),
                        artifactId = ArtifactId("datastore-preferences-core"),
                        version = Version("1.0.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2022-3171"), Vulnerability("CVE-2022-3510")),
                ),
            )
        )

        val actual = underTest.suppressVulnerableDependencies(report, suppressions)

        actual.dependencies shouldBe emptyList()
    }

    test("suppress just some of the vulnerabilities from multiple dependencies") {
        val suppressions = listOf(
            createVulnerabilitySuppression(
                fullyQualifiedName = "com.squareup.retrofit2:retrofit",
                vulnerabilities = listOf("CVE-2018-1000844"),
            ),
            createVulnerabilitySuppression(
                fullyQualifiedName = "androidx.datastore:datastore-preferences-core",
                vulnerabilities = listOf("CVE-2022-3171"),
            ),
        )
        val retrofitDependencyName = DependencyName(
            groupId = GroupId("com.squareup.retrofit2"),
            artifactId = ArtifactId("retrofit"),
            version = Version("2.4.0"),
        )
        val datastoreCoreDependencyName = DependencyName(
            groupId = GroupId("androidx.datastore"),
            artifactId = ArtifactId("datastore-preferences-core"),
            version = Version("1.0.0"),
        )
        val datastorePreferencesVulnerableDependency = VulnerableDependency(
            name = DependencyName(
                groupId = GroupId("androidx.datastore"),
                artifactId = ArtifactId("datastore-preferences"),
                version = Version("1.0.0"),
            ),
            vulnerabilities = listOf(Vulnerability("CVE-2022-3171")),
        )
        val report = VulnerabilitiesReport(
            dependencies = listOf(
                VulnerableDependency(
                    name = retrofitDependencyName,
                    vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2018-1000850")),
                ),
                VulnerableDependency(
                    name = datastoreCoreDependencyName,
                    vulnerabilities = listOf(Vulnerability("CVE-2022-3171"), Vulnerability("CVE-2022-3510")),
                ),
                datastorePreferencesVulnerableDependency,
            )
        )

        val actual = underTest.suppressVulnerableDependencies(report, suppressions)

        actual.dependencies shouldBe listOf(
            VulnerableDependency(
                name = retrofitDependencyName,
                vulnerabilities = listOf(Vulnerability("CVE-2018-1000850")),
            ),
            VulnerableDependency(
                name = datastoreCoreDependencyName,
                vulnerabilities = listOf(Vulnerability("CVE-2022-3510")),
            ),
            datastorePreferencesVulnerableDependency,
        )
    }

    test("do not suppress anything if no suppressions specified") {
        val suppressions = emptyList<VulnerabilitySuppression>()
        val expected = VulnerabilitiesReport(
            dependencies = listOf(createVulnerableDependency()),
        )

        val actual = underTest.suppressVulnerableDependencies(expected, suppressions)

        actual shouldBe expected
    }

    test("report warnings when there are unused suppressions") {
        val expectedFullyQualifiedName1 = "com.squareup.retrofit2:retrofit"
        val expectedUnusedSuppression1 = "CVE-2018-1000850"
        val expectedFullyQualifiedName2 = "androidx.datastore:datastore-preferences-core"
        val expectedUnusedSuppression2 = "CVE-2022-3510"
        val suppressions = listOf(
            createVulnerabilitySuppression(
                fullyQualifiedName = expectedFullyQualifiedName1,
                vulnerabilities = listOf("CVE-2018-1000844", expectedUnusedSuppression1),
            ),
            createVulnerabilitySuppression(
                fullyQualifiedName = expectedFullyQualifiedName2,
                vulnerabilities = listOf("CVE-2022-3171", expectedUnusedSuppression2),
            ),
        )
        val report = VulnerabilitiesReport(
            dependencies = listOf(
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("com.squareup.retrofit2"),
                        artifactId = ArtifactId("retrofit"),
                        version = Version("2.4.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2018-1000844")),
                ),
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("androidx.datastore"),
                        artifactId = ArtifactId("datastore-preferences-core"),
                        version = Version("1.0.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2022-3171")),
                ),
            )
        )

        underTest.suppressVulnerableDependencies(report, suppressions)

        val actualWarnings = dangerContext.warnings.map { it.message }
        actualWarnings shouldHaveSize 2
        actualWarnings shouldContain Suppressor.createUnusedVulnerabilitySuppressionMessage(
            fullyQualifiedName = expectedFullyQualifiedName1,
            vulnerability = expectedUnusedSuppression1,
        )
        actualWarnings shouldContain Suppressor.createUnusedVulnerabilitySuppressionMessage(
            fullyQualifiedName = expectedFullyQualifiedName2,
            vulnerability = expectedUnusedSuppression2,
        )
    }

    test("do not report any warnings when there are no unused suppressions") {
        val suppressions = listOf(
            createVulnerabilitySuppression(
                fullyQualifiedName = "com.squareup.retrofit2:retrofit",
                vulnerabilities = listOf("CVE-2018-1000844", "CVE-2018-1000850"),
            ),
            createVulnerabilitySuppression(
                fullyQualifiedName = "androidx.datastore:datastore-preferences-core",
                vulnerabilities = listOf("CVE-2022-3171", "CVE-2022-3510"),
            ),
        )
        val report = VulnerabilitiesReport(
            dependencies = listOf(
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("com.squareup.retrofit2"),
                        artifactId = ArtifactId("retrofit"),
                        version = Version("2.4.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2018-1000844"), Vulnerability("CVE-2018-1000850")),
                ),
                VulnerableDependency(
                    name = DependencyName(
                        groupId = GroupId("androidx.datastore"),
                        artifactId = ArtifactId("datastore-preferences-core"),
                        version = Version("1.0.0"),
                    ),
                    vulnerabilities = listOf(Vulnerability("CVE-2022-3171"), Vulnerability("CVE-2022-3510")),
                ),
            )
        )

        underTest.suppressVulnerableDependencies(report, suppressions)

        dangerContext.warnings shouldHaveSize 0
    }
}

private fun FunSpec.testOutdatedDependencies() = context("outdated dependencies") {
    test("do not suppress anything if no suppressions specified") {
        val suppressions = emptyList<OutdatedDependencySuppression>()
        val expected = DependenciesUpdateReport(
            outdatedDependencies = listOf(createOutdatedDependency()),
            upToDateDependencies = listOf(createUpToDateDependency()),
        )

        val actual = underTest.suppressOutdatedDependencies(expected, suppressions)

        actual shouldBe expected
    }

    test("suppress updates of multiple dependencies") {
        val retrofitDependencyName = DependencyName(
            groupId = GroupId("com.squareup.retrofit2"),
            artifactId = ArtifactId("retrofit"),
            version = Version("2.4.0"),
        )
        val datastoreDependencyName = DependencyName(
            groupId = GroupId("androidx.datastore"),
            artifactId = ArtifactId("datastore-preferences-core"),
            version = Version("1.0.0"),
        )
        val suppressions = listOf(
            createOutdatedDependencySuppression(
                fullyQualifiedNameWithVersion = retrofitDependencyName.fullyQualifiedNameWithVersion,
            ),
            createOutdatedDependencySuppression(
                fullyQualifiedNameWithVersion = datastoreDependencyName.fullyQualifiedNameWithVersion,
            ),
        )
        val expectedOutdatedDependency = OutdatedDependency(
            name = DependencyName(
                groupId = GroupId("androidx.compose"),
                artifactId = ArtifactId("compose-bom"),
                version = Version("2024.01.00"),
            ),
            newestAvailableVersion = Version("2024.02.01"),
        )
        val expectedUpToDateDependencies = listOf(
            UpToDateDependency(
                DependencyName(
                    groupId = GroupId("org.jetbrains.kotlin"),
                    artifactId = ArtifactId("kotlin-stdlib"),
                    version = Version("1.9.22"),
                )
            )
        )
        val report = DependenciesUpdateReport(
            outdatedDependencies = listOf(
                OutdatedDependency(
                    name = retrofitDependencyName,
                    newestAvailableVersion = Version("2.9.0"),
                ),
                OutdatedDependency(
                    name = datastoreDependencyName,
                    newestAvailableVersion = Version("1.1.0"),
                ),
                expectedOutdatedDependency,
            ),
            upToDateDependencies = expectedUpToDateDependencies,
        )

        val actual = underTest.suppressOutdatedDependencies(report, suppressions)

        actual shouldBe DependenciesUpdateReport(
            outdatedDependencies = listOf(expectedOutdatedDependency),
            upToDateDependencies = expectedUpToDateDependencies,
        )
    }

    test("report warnings when there are unused suppressions") {
        val expectedUnusedSuppressionDependencyName1 = DependencyName(
            groupId = GroupId("com.squareup.retrofit2"),
            artifactId = ArtifactId("retrofit"),
            version = Version("2.4.0"),
        )
        val expectedUnusedSuppressionDependencyName2 = DependencyName(
            groupId = GroupId("androidx.datastore"),
            artifactId = ArtifactId("datastore-preferences-core"),
            version = Version("1.0.0"),
        )
        val suppressedDependencyName = DependencyName(
            groupId = GroupId("org.jetbrains.kotlin"),
            artifactId = ArtifactId("kotlin-stdlib"),
            version = Version("1.9.22"),
        )
        val suppressions = listOf(
            createOutdatedDependencySuppression(
                fullyQualifiedNameWithVersion = suppressedDependencyName.fullyQualifiedNameWithVersion,
            ),
            createOutdatedDependencySuppression(
                fullyQualifiedNameWithVersion = expectedUnusedSuppressionDependencyName1.fullyQualifiedNameWithVersion,
            ),
            createOutdatedDependencySuppression(
                fullyQualifiedNameWithVersion = expectedUnusedSuppressionDependencyName2.fullyQualifiedNameWithVersion,
            ),
        )
        val report = DependenciesUpdateReport(
            outdatedDependencies = listOf(
                OutdatedDependency(
                    name = suppressedDependencyName,
                    newestAvailableVersion = Version("2.0.0"),
                ),
            ),
            upToDateDependencies = emptyList(),
        )

        underTest.suppressOutdatedDependencies(report, suppressions)

        val actualWarnings = dangerContext.warnings.map { it.message }
        actualWarnings shouldHaveSize 2
        actualWarnings shouldContain Suppressor.createUnusedOutdatedDependencySuppressionMessage(
            fullyQualifiedNameWithVersion = expectedUnusedSuppressionDependencyName1.fullyQualifiedNameWithVersion,
        )
        actualWarnings shouldContain Suppressor.createUnusedOutdatedDependencySuppressionMessage(
            fullyQualifiedNameWithVersion = expectedUnusedSuppressionDependencyName2.fullyQualifiedNameWithVersion,
        )
    }

    test("do not report any warnings when there are no unused suppressions") {
        val suppressedDependencyName = DependencyName(
            groupId = GroupId("org.jetbrains.kotlin"),
            artifactId = ArtifactId("kotlin-stdlib"),
            version = Version("1.9.22"),
        )
        val suppressions = listOf(
            createOutdatedDependencySuppression(fullyQualifiedNameWithVersion = suppressedDependencyName.fullyQualifiedNameWithVersion),
        )
        val report = DependenciesUpdateReport(
            outdatedDependencies = listOf(
                OutdatedDependency(
                    name = suppressedDependencyName,
                    newestAvailableVersion = Version("2.0.0"),
                ),
            ),
            upToDateDependencies = emptyList(),
        )

        underTest.suppressOutdatedDependencies(report, suppressions)

        dangerContext.warnings shouldHaveSize 0
    }
}
