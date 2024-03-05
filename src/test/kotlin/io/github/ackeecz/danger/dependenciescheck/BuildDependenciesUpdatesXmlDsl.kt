package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlOutdatedDependency
import io.github.ackeecz.danger.dependenciescheck.model.xml.update.XmlUpToDateDependency
import io.github.ackeecz.danger.dependenciescheck.util.XmlElement
import io.github.ackeecz.danger.dependenciescheck.util.buildXmlFile
import io.kotest.core.TestConfiguration
import java.io.File

internal fun TestConfiguration.buildDependenciesUpdatesXmlFile(block: ResponseElementBuilder.() -> Unit): File {
    return buildXmlFile("response") {
        ResponseElementBuilder(this).apply(block)
    }
}

internal class ResponseElementBuilder(private val responseElement: XmlElement) {

    fun currentDependencies(block: CurrentDependenciesBuilder.() -> Unit) {
        responseElement.element("current") {
            element("dependencies") {
                CurrentDependenciesBuilder(this).apply(block)
            }
        }
    }

    fun emptyCurrentDependencies() {
        currentDependencies {}
    }

    fun outdatedDependencies(block: OutdatedDependenciesBuilder.() -> Unit) {
        responseElement.element("outdated") {
            element("dependencies") {
                OutdatedDependenciesBuilder(this).apply(block)
            }
        }
    }

    fun emptyOutdatedDependencies() {
        outdatedDependencies {}
    }
}

internal class CurrentDependenciesBuilder(private val currentDependenciesElement: XmlElement) {

    fun dependency(block: CurrentDependencyBuilder.() -> Unit) {
        currentDependenciesElement.element("dependency") {
            CurrentDependencyBuilder().apply(block).build(this)
        }
    }

    fun dependency(dependency: XmlUpToDateDependency) {
        dependency {
            group = dependency.groupId
            name = dependency.artifactId
            version = dependency.currentVersion
        }
    }
}

internal class CurrentDependencyBuilder : DependencyBuilder()

internal abstract class DependencyBuilder {

    var group: String? = null
    var name: String? = null
    var version: String? = null

    open fun build(dependencyElement: XmlElement) {
        with(dependencyElement) {
            element("group", group!!)
            element("name", name!!)
            element("version", version!!)
        }
    }
}

internal class OutdatedDependenciesBuilder(private val outdatedDependenciesElement: XmlElement) {

    fun outdatedDependency(block: OutdatedDependencyBuilder.() -> Unit) {
        outdatedDependenciesElement.element("outdatedDependency") {
            OutdatedDependencyBuilder().apply(block).build(this)
        }
    }

    fun outdatedDependency(dependency: XmlOutdatedDependency) {
        outdatedDependency {
            group = dependency.groupId
            name = dependency.artifactId
            version = dependency.currentVersion
            availableMilestone = dependency.availableVersion.version
        }
    }
}

internal class OutdatedDependencyBuilder : DependencyBuilder() {

    var availableMilestone: String? = null

    override fun build(dependencyElement: XmlElement) {
        super.build(dependencyElement)
        dependencyElement.element("available") {
            element("milestone", availableMilestone!!)
        }
    }
}
