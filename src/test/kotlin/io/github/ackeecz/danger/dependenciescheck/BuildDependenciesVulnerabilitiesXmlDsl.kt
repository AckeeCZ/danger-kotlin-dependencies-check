package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.model.xml.vulnerability.XmlVulnerabilityReportDependency
import io.github.ackeecz.danger.dependenciescheck.util.XmlElement
import io.github.ackeecz.danger.dependenciescheck.util.buildXmlFile
import io.kotest.core.TestConfiguration
import java.io.File

internal fun TestConfiguration.buildDependenciesVulnerabilitiesXmlFile(block: DependenciesElementBuilder.() -> Unit): File {
    return buildXmlFile("analysis") {
        element("dependencies") {
            DependenciesElementBuilder(this).apply(block)
        }
    }
}

internal class DependenciesElementBuilder(private val dependenciesElement: XmlElement) {

    fun dependency(block: VulnerableDependencyBuilder.() -> Unit) {
        dependenciesElement.element("dependency") {
            VulnerableDependencyBuilder().apply(block).build(this)
        }
    }

    fun dependency(dependency: XmlVulnerabilityReportDependency) {
        dependency {
            fileName = dependency.fileName
            filePath = dependency.filePath
            dependency.vulnerabilities.forEach { vulnerability(it.name) }
        }
    }
}

internal class VulnerableDependencyBuilder {

    var fileName: String? = null
    var filePath: String? = null
    private val vulnerabilities = mutableListOf<String>()

    fun vulnerability(name: String) {
        vulnerabilities += name
    }

    fun build(dependencyElement: XmlElement) {
        with(dependencyElement) {
            element("fileName", fileName!!)
            element("filePath", filePath!!)
            if (vulnerabilities.isNotEmpty()) {
                element("vulnerabilities") {
                    vulnerabilities.forEach { vulnerability ->
                        element("vulnerability") {
                            element("name", vulnerability)
                            // For testing extra properties during deserialization
                            element("severity", "HIGH")
                        }
                    }
                }
            }
            // For testing extra properties during deserialization
            element("md5", "ae447128b0a0625524178873c6b9aa12")
        }
    }
}
