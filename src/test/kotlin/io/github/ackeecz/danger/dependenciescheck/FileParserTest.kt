@file:Suppress("MaximumLineLength")

package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlAvailableVersion
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlOutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlUpToDateDependency
import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerability
import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerabilityReportDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe

private lateinit var underTest: FileParser

internal class FileParserTest : FunSpec({

    beforeEach {
        underTest = FileParser()
    }

    testDependenciesUpdates()
    testVulnerabilities()
})

private fun FunSpec.testDependenciesUpdates() = context("dependencies updates") {
    test("parse dependencies update report from multiple files") {
        // Arrange
        val firstUpToDateDependency = XmlUpToDateDependency(
            groupId = "androidx.appcompat",
            artifactId = "appcompat",
            currentVersion = "1.6.1",
        )
        val secondUpToDateDependency = XmlUpToDateDependency(
            groupId = "androidx.core",
            artifactId = "core-ktx",
            currentVersion = "1.12.0",
        )
        val firstOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.activity",
            artifactId = "activity-compose",
            currentVersion = "1.7.2",
            availableVersion = XmlAvailableVersion(version = "1.8.2"),
        )
        val secondOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.compose",
            artifactId = "compose-bom",
            currentVersion = "2023.09.00",
            availableVersion = XmlAvailableVersion(version = "2024.02.01"),
        )
        val firstFile = buildDependenciesUpdatesXmlFile {
            currentDependencies {
                dependency(firstUpToDateDependency)
                dependency(secondUpToDateDependency)
            }
            outdatedDependencies {
                outdatedDependency(firstOutdatedDependency)
                outdatedDependency(secondOutdatedDependency)
            }
        }

        val thirdUpToDateDependency = XmlUpToDateDependency(
            groupId = "app.cash.turbine",
            artifactId = "turbine",
            currentVersion = "1.0.0",
        )
        val fourthUpToDateDependency = XmlUpToDateDependency(
            groupId = "com.google.android.material",
            artifactId = "material",
            currentVersion = "1.11.0",
        )
        val thirdOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.lifecycle",
            artifactId = "lifecycle-runtime-compose",
            currentVersion = "2.6.2",
            availableVersion = XmlAvailableVersion(version = "2.7.0"),
        )
        val fourthOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.navigation",
            artifactId = "navigation-fragment-ktx",
            currentVersion = "2.7.2",
            availableVersion = XmlAvailableVersion(version = "2.7.7"),
        )
        val secondFile = buildDependenciesUpdatesXmlFile {
            currentDependencies {
                dependency(thirdUpToDateDependency)
                dependency(fourthUpToDateDependency)
            }
            outdatedDependencies {
                outdatedDependency(thirdOutdatedDependency)
                outdatedDependency(fourthOutdatedDependency)
            }
        }

        // Act
        val actual = underTest.parseUpdates(listOf(firstFile, secondFile))

        // Assert
        with(actual.upToDateDependencies) {
            size shouldBe 4
            this shouldContain firstUpToDateDependency.toUpToDateDependency()
            this shouldContain secondUpToDateDependency.toUpToDateDependency()
            this shouldContain thirdUpToDateDependency.toUpToDateDependency()
            this shouldContain fourthUpToDateDependency.toUpToDateDependency()
        }
        with(actual.outdatedDependencies) {
            size shouldBe 4
            this shouldContain firstOutdatedDependency.toOutdatedDependency()
            this shouldContain secondOutdatedDependency.toOutdatedDependency()
            this shouldContain thirdOutdatedDependency.toOutdatedDependency()
            this shouldContain fourthOutdatedDependency.toOutdatedDependency()
        }
    }

    test("remove parsed duplicated versions") {
        // Arrange
        val duplicatedUpToDateDependency = XmlUpToDateDependency(
            groupId = "androidx.appcompat",
            artifactId = "appcompat",
            currentVersion = "1.6.1",
        )
        val notDuplicatedUpToDateDependency = XmlUpToDateDependency(
            groupId = "androidx.core",
            artifactId = "core-ktx",
            currentVersion = "1.12.0",
        )

        val duplicatedOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.activity",
            artifactId = "activity-compose",
            currentVersion = "1.7.2",
            availableVersion = XmlAvailableVersion(version = "1.8.2"),
        )
        val firstNotDuplicatedOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.compose",
            artifactId = "compose-bom",
            currentVersion = "2023.09.00",
            availableVersion = XmlAvailableVersion(version = "2024.02.01"),
        )
        val secondNotDuplicatedOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.compose",
            artifactId = "compose-bom",
            currentVersion = "2023.10.00",
            availableVersion = XmlAvailableVersion(version = "2024.02.01"),
        )

        val firstFile = buildDependenciesUpdatesXmlFile {
            currentDependencies {
                dependency(duplicatedUpToDateDependency)
                dependency(notDuplicatedUpToDateDependency)
            }
            outdatedDependencies {
                outdatedDependency(duplicatedOutdatedDependency)
                outdatedDependency(firstNotDuplicatedOutdatedDependency)
            }
        }
        val secondFile = buildDependenciesUpdatesXmlFile {
            currentDependencies {
                dependency(duplicatedUpToDateDependency)
            }
            outdatedDependencies {
                outdatedDependency(duplicatedOutdatedDependency)
                outdatedDependency(secondNotDuplicatedOutdatedDependency)
            }
        }

        // Act
        val actual = underTest.parseUpdates(listOf(firstFile, secondFile))

        // Assert
        with(actual.upToDateDependencies) {
            size shouldBe 2
            this shouldContain duplicatedUpToDateDependency.toUpToDateDependency()
            this shouldContain notDuplicatedUpToDateDependency.toUpToDateDependency()
        }
        with(actual.outdatedDependencies) {
            size shouldBe 3
            this shouldContain duplicatedOutdatedDependency.toOutdatedDependency()
            this shouldContain firstNotDuplicatedOutdatedDependency.toOutdatedDependency()
            this shouldContain secondNotDuplicatedOutdatedDependency.toOutdatedDependency()
        }
    }
}

private fun FunSpec.testVulnerabilities() = context("vulnerabilities") {
    test("parse dependencies vulnerabilities report from multiple files") {
        // Assert
        val firstDependency = XmlVulnerabilityReportDependency(
            fileName = "okio-1.14.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/1.14.0/102d7be47241d781ef95f1581d414b0943053130/okio-1.14.0.jar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2023-3635")),
        )
        val secondDependency = XmlVulnerabilityReportDependency(
            fileName = "retrofit-2.4.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.retrofit2/retrofit/2.4.0/fc4aa382632bfaa7be7b41579efba41d5a71ecf3/retrofit-2.4.0.jar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2018-1000844"), XmlVulnerability("CVE-2018-1000850")),
        )
        val firstFile = buildDependenciesVulnerabilitiesXmlFile {
            dependency(firstDependency)
            dependency(secondDependency)
        }

        val thirdDependency = XmlVulnerabilityReportDependency(
            fileName = "activity-1.8.0.aar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/androidx.activity/activity/1.8.0/4266e2118d565daa20212d1726e11f41e1a4d0ca/activity-1.8.0.aar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2023-1000786")),
        )
        val secondFile = buildDependenciesVulnerabilitiesXmlFile {
            dependency(thirdDependency)
        }

        // Act
        val actual = underTest.parseVulnerabilities(listOf(firstFile, secondFile))

        // Assert
        val actualDependencies = actual.dependencies
        actualDependencies.size shouldBe 3
        actualDependencies shouldContain firstDependency.toVulnerableDependency()
        actualDependencies shouldContain secondDependency.toVulnerableDependency()
        actualDependencies shouldContain thirdDependency.toVulnerableDependency()
    }

    test("filter out dependency with no vulnerabilities") {
        val dependency = XmlVulnerabilityReportDependency(
            fileName = "okio-1.14.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/1.14.0/102d7be47241d781ef95f1581d414b0943053130/okio-1.14.0.jar",
            vulnerabilities = emptyList(),
        )
        val file = buildDependenciesVulnerabilitiesXmlFile { dependency(dependency) }

        val actual = underTest.parseVulnerabilities(listOf(file))

        actual.dependencies.size shouldBe 0
    }

    test("remove duplicated vulnerable dependencies") {
        val dependency = XmlVulnerabilityReportDependency(
            fileName = "okio-1.14.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/1.14.0/102d7be47241d781ef95f1581d414b0943053130/okio-1.14.0.jar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2023-1000786")),
        )
        val firstFile = buildDependenciesVulnerabilitiesXmlFile { dependency(dependency) }
        val secondFile = buildDependenciesVulnerabilitiesXmlFile { dependency(dependency) }

        val actual = underTest.parseVulnerabilities(listOf(firstFile, secondFile))

        actual.dependencies.size shouldBe 1
        actual.dependencies shouldContain dependency.toVulnerableDependency()
    }
}
