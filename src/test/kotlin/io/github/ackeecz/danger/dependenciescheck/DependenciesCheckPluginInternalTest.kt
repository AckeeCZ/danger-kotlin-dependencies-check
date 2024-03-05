@file:Suppress("MaximumLineLength")

package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.config.Config
import io.github.ackeecz.danger.dependenciescheck.config.OutdatedDependencySuppression
import io.github.ackeecz.danger.dependenciescheck.config.VulnerabilitySuppression
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlAvailableVersion
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlOutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerability
import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerabilityReportDependency
import io.github.ackeecz.danger.dependenciescheck.util.rootFileTestDir
import io.github.ackeecz.danger.dependenciescheck.util.tempdir
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import systems.danger.kotlin.sdk.DangerContext
import java.io.File
import java.util.*

private lateinit var dangerContext: DangerContext
private lateinit var commandLine: FakeCommandLine

private lateinit var underTest: DependenciesCheckPluginInternal

internal class DependenciesCheckPluginInternalTest : FunSpec({

    beforeEach {
        dangerContext = FakeDangerContext()
        commandLine = FakeCommandLine()

        underTest = DependenciesCheckPluginInternal(dangerContext, commandLine)
    }

    test("report found outdated dependencies with suppression support") {
        // Arrange
        val firstXmlOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.lifecycle",
            artifactId = "lifecycle-runtime-compose",
            currentVersion = "2.6.2",
            availableVersion = XmlAvailableVersion(version = "2.7.0"),
        )
        val secondXmlOutdatedDependency = XmlOutdatedDependency(
            groupId = "androidx.compose",
            artifactId = "compose-bom",
            currentVersion = "2023.09.00",
            availableVersion = XmlAvailableVersion(version = "2024.02.01"),
        )
        val expectedOutdatedDependency = firstXmlOutdatedDependency.toOutdatedDependency()
        val suppressedOutdatedDependency = secondXmlOutdatedDependency.toOutdatedDependency()

        val updatesReportFileName = generateFileName(baseFileName = "custom-updates-report-file-name")
        buildDependenciesUpdatesXmlFile {
            emptyCurrentDependencies()
            outdatedDependencies {
                outdatedDependency(firstXmlOutdatedDependency)
                outdatedDependency(secondXmlOutdatedDependency)
            }
        }.moveToExpectedFile(updatesReportFileName)
        val vulnerabilitiesReportFileName = generateFileName(baseFileName = "custom-vulnerability-report-file-name.xml")
        arrangeValidVulnerabilitiesFile(vulnerabilitiesReportFileName)

        val config = Config(
            outdatedDependenciesConfig = Config.OutdatedDependencies(
                reportFileName = updatesReportFileName,
                suppressions = listOf(
                    OutdatedDependencySuppression(suppressedOutdatedDependency.name.fullyQualifiedNameWithVersion),
                ),
            ),
            vulnerabilitiesConfig = Config.Vulnerabilities(reportFileName = vulnerabilitiesReportFileName),
        )

        // Act
        underTest.checkDependencies(config)

        // Assert
        val warnings = dangerContext.warnings.map { it.message }
        warnings shouldHaveSize 1
        warnings.first() shouldBe Reporter.createOutdatedDependencyMessage(expectedOutdatedDependency)
    }

    test("report found vulnerable dependency with suppression support") {
        // Assert
        val firstXmlVulnerableDependency = XmlVulnerabilityReportDependency(
            fileName = "okio-1.14.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.okio/okio/1.14.0/102d7be47241d781ef95f1581d414b0943053130/okio-1.14.0.jar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2023-3635")),
        )
        val secondXmlVulnerableDependency = XmlVulnerabilityReportDependency(
            fileName = "retrofit-2.4.0.jar",
            filePath = "/Users/janmottl/.gradle/caches/modules-2/files-2.1/com.squareup.retrofit2/retrofit/2.4.0/fc4aa382632bfaa7be7b41579efba41d5a71ecf3/retrofit-2.4.0.jar",
            vulnerabilities = listOf(XmlVulnerability("CVE-2018-1000844"), XmlVulnerability("CVE-2018-1000850")),
        )
        val suppressedDependency = firstXmlVulnerableDependency.toVulnerableDependency()
        val expectedReportedDependency = secondXmlVulnerableDependency.toVulnerableDependency()

        val vulnerabilitiesReportFileName = generateFileName(baseFileName = "custom-vulnerability-report-file-name.xml")
        buildDependenciesVulnerabilitiesXmlFile {
            dependency(firstXmlVulnerableDependency)
            dependency(secondXmlVulnerableDependency)
        }.moveToExpectedFile(vulnerabilitiesReportFileName)
        val updatesReportFileName = generateFileName(baseFileName = "custom-updates-report-file-name.xml")
        arrangeValidDependenciesUpdatesFile(updatesReportFileName)

        val config = Config(
            vulnerabilitiesConfig = Config.Vulnerabilities(
                reportFileName = vulnerabilitiesReportFileName,
                suppressions = listOf(
                    VulnerabilitySuppression(
                        groupId = suppressedDependency.name.groupId.value,
                        artifactId = suppressedDependency.name.artifactId.value,
                        vulnerabilities = suppressedDependency.vulnerabilities.map { it.value },
                    ),
                ),
            ),
            outdatedDependenciesConfig = Config.OutdatedDependencies(reportFileName = updatesReportFileName),
        )

        // Act
        underTest.checkDependencies(config)

        // Assert
        val warnings = dangerContext.warnings.map { it.message }
        warnings shouldHaveSize 1
        warnings.first() shouldBe Reporter.createVulnerableTransitiveDependencyMessage(expectedReportedDependency)
    }

    fun testCommandExecution(expectedCommand: String) {
        val vulnerabilitiesFileName = generateFileName("vulnerabilities")
        val updatesFileName = generateFileName("updates")
        arrangeValidVulnerabilitiesFile(vulnerabilitiesFileName)
        arrangeValidDependenciesUpdatesFile(updatesFileName)

        underTest.checkDependencies(
            Config(
                vulnerabilitiesConfig = Config.Vulnerabilities(reportFileName = vulnerabilitiesFileName),
                outdatedDependenciesConfig = Config.OutdatedDependencies(reportFileName = updatesFileName),
            )
        )

        commandLine.executedCommands shouldContain expectedCommand
    }

    test("execute command for dependencies vulnerabilities check") {
        testCommandExecution(expectedCommand = "./gradlew dependencyCheckAnalyze --info")
    }

    test("execute command for dependencies updates check") {
        testCommandExecution(expectedCommand = "./gradlew dependencyUpdates")
    }
})

// We need to have distinct file names per Spec because temp files are cleaned up after whole Spec and not after each test
private fun generateFileName(baseFileName: String) = "$baseFileName-${UUID.randomUUID()}.xml"

context(TestConfiguration)
private fun File.moveToExpectedFile(fileName: String) {
    val rootDir = rootFileTestDir
    val testDir = tempdir(rootDir, "test")
    File(testDir, fileName).also {
        this.copyTo(it, overwrite = true)
    }
}

private fun TestConfiguration.arrangeValidVulnerabilitiesFile(fileName: String) {
    buildDependenciesVulnerabilitiesXmlFile {}.moveToExpectedFile(fileName)
}

private fun TestConfiguration.arrangeValidDependenciesUpdatesFile(fileName: String) {
    buildDependenciesUpdatesXmlFile {
        emptyCurrentDependencies()
        emptyOutdatedDependencies()
    }.moveToExpectedFile(fileName)
}
