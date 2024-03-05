package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class XmlAvailableVersion(
    @field:JacksonXmlProperty(localName = "milestone")
    val version: String,
)
