package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
internal data class XmlDependenciesUpdateReport(
    @field:JacksonXmlProperty(localName = "outdated")
    val outdatedDependencies: XmlOutdatedDependencies,
    @field:JacksonXmlProperty(localName = "current")
    val upToDateDependencies: XmlUpToDateDependencies,
)
