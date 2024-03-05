package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class XmlUpToDateDependencies(
    @field:JacksonXmlElementWrapper(localName = "dependencies")
    val dependencies: List<XmlUpToDateDependency>,
)
