package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.config.Config
import io.github.ackeecz.danger.dependenciescheck.model.update.DependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerabilitiesReport
import systems.danger.kotlin.sdk.DangerContext
import systems.danger.kotlin.sdk.DangerPlugin
import java.io.File
import java.nio.file.Paths

/**
 * Plugin for danger-kotlin for checking project dependencies (e.g. new available updates or vulnerabilities). Plugin
 * relies on these Gradle plugins to be available on the project:
 * - https://github.com/jeremylong/DependencyCheck
 * - https://github.com/ben-manes/gradle-versions-plugin
 */
public object DependenciesCheckPlugin : DangerPlugin() {

    override val id: String = "io.github.ackeecz.danger-kotlin-dependencies-check"

    private val commandLine = CommandLineImpl()

    public fun checkDependencies(config: Config = Config()) {
        DependenciesCheckPluginInternal(
            dangerContext = context,
            commandLine = commandLine,
        ).checkDependencies(config)
    }
}

internal class DependenciesCheckPluginInternal(
    dangerContext: DangerContext,
    private val commandLine: CommandLine,
) {

    private val fileFinder = FileFinder()
    private val fileParser = FileParser()
    private val suppressor = Suppressor(dangerContext = dangerContext)
    private val reporter = Reporter(dangerContext = dangerContext)

    fun checkDependencies(config: Config) {
        executeGradleTasks(config)
        processTasksResults(config)
    }

    private fun executeGradleTasks(config: Config) {
        commandLine.execute("./gradlew ${config.vulnerabilitiesConfig.gradleTaskNameWithArgs.value}")
        commandLine.execute("./gradlew ${config.outdatedDependenciesConfig.gradleTaskNameWithArgs.value}")
    }

    private fun processTasksResults(config: Config) {
        val vulnerabilitiesReport = parseVulnerabilitiesReport(config.vulnerabilitiesConfig)
            .applySuppressions(config.vulnerabilitiesConfig)
        val updatesReport = parseUpdatesReport(config.outdatedDependenciesConfig)
            .applySuppressions(config.outdatedDependenciesConfig)
        reporter.reportVulnerable(
            vulnerableDependencies = vulnerabilitiesReport.dependencies,
            dependenciesUpdateReport = updatesReport,
        )
        reporter.reportOutdated(updatesReport.outdatedDependencies)
    }

    private fun parseUpdatesReport(config: Config.OutdatedDependencies): DependenciesUpdateReport {
        return fileParser.parseUpdates(findFiles(fileName = config.reportFileName))
    }

    private fun findFiles(fileName: String): List<File> {
        return fileFinder.findFiles(
            rootDirectoryPath = Paths.get(""),
            fileName = fileName,
        )
    }

    private fun DependenciesUpdateReport.applySuppressions(config: Config.OutdatedDependencies): DependenciesUpdateReport {
        return suppressor.suppressOutdatedDependencies(report = this, suppressions = config.suppressions)
    }

    private fun parseVulnerabilitiesReport(config: Config.Vulnerabilities): VulnerabilitiesReport {
        return fileParser.parseVulnerabilities(findFiles(fileName = config.reportFileName))
    }

    private fun VulnerabilitiesReport.applySuppressions(config: Config.Vulnerabilities): VulnerabilitiesReport {
        return suppressor.suppressVulnerableDependencies(report = this, suppressions = config.suppressions)
    }
}
