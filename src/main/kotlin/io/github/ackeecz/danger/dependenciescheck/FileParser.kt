package io.github.ackeecz.danger.dependenciescheck

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.ackeecz.danger.dependenciescheck.model.update.DependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.vulnerability.VulnerabilitiesReport
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlDependenciesUpdateReport
import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerabilitiesReport
import java.io.File

internal class FileParser {

    private val xmlMapper = XmlMapper().registerKotlinModule()

    fun parseUpdates(files: List<File>): DependenciesUpdateReport {
        return files.map { file -> xmlMapper.readValue<XmlDependenciesUpdateReport>(file.inputStream()) }
            .map { report ->
                DependenciesUpdateReport(
                    outdatedDependencies = report.outdatedDependencies.dependencies.map {
                        it.toOutdatedDependency()
                    },
                    upToDateDependencies = report.upToDateDependencies.dependencies.map {
                        it.toUpToDateDependency()
                    },
                )
            }
            .reduce { accumulator, current ->
                with(accumulator) {
                    copy(
                        outdatedDependencies = outdatedDependencies + current.outdatedDependencies,
                        upToDateDependencies = upToDateDependencies + current.upToDateDependencies,
                    )
                }
            }
            .removeDuplicates()
    }

    private fun DependenciesUpdateReport.removeDuplicates(): DependenciesUpdateReport {
        return copy(
            outdatedDependencies = outdatedDependencies.distinct(),
            upToDateDependencies = upToDateDependencies.distinct(),
        )
    }

    fun parseVulnerabilities(files: List<File>): VulnerabilitiesReport {
        return files.asSequence().map { file -> xmlMapper.readValue<XmlVulnerabilitiesReport>(file.inputStream()) }
            .map { report ->
                VulnerabilitiesReport(
                    dependencies = report.dependencies.map { it.toVulnerableDependency() },
                )
            }
            .flatMap { it.dependencies }
            .filter { it.vulnerabilities.isNotEmpty() }
            .distinct()
            .toList()
            .let { VulnerabilitiesReport(dependencies = it) }
    }
}
