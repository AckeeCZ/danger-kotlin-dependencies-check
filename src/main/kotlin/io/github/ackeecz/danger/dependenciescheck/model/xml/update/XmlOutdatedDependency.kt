package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import io.github.ackeecz.danger.dependenciescheck.model.ArtifactId
import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.OutdatedDependency

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class XmlOutdatedDependency(
    @field:JacksonXmlProperty(localName = "group")
    val groupId: String,
    @field:JacksonXmlProperty(localName = "name")
    val artifactId: String,
    @field:JacksonXmlProperty(localName = "version")
    val currentVersion: String,
    @field:JacksonXmlProperty(localName = "available")
    val availableVersion: XmlAvailableVersion,
) {

    fun toOutdatedDependency() = OutdatedDependency(
        name = DependencyName(
            groupId = GroupId(groupId),
            artifactId = ArtifactId(artifactId),
            version = Version(currentVersion),
        ),
        newestAvailableVersion = Version(availableVersion.version),
    )
}
