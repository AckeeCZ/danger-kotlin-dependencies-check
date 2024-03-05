package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import io.github.ackeecz.danger.dependenciescheck.model.ArtifactId
import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.UpToDateDependency

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class XmlUpToDateDependency(
    @field:JacksonXmlProperty(localName = "group")
    val groupId: String,
    @field:JacksonXmlProperty(localName = "name")
    val artifactId: String,
    @field:JacksonXmlProperty(localName = "version")
    val currentVersion: String,
) {

    fun toUpToDateDependency() = UpToDateDependency(
        name = DependencyName(
            groupId = GroupId(groupId),
            artifactId = ArtifactId(artifactId),
            version = Version(currentVersion),
        ),
    )
}
