package io.github.ackeecz.danger.dependenciescheck.util

import io.kotest.core.TestConfiguration
import java.io.File

internal fun TestConfiguration.buildXmlFile(rootElementName: String, block: XmlElement.() -> Unit): File {
    val serializedXml = XmlElementImpl(rootElementName).apply(block)
        .serialize()
        .let { xmlElements ->
            val firstLine = """<?xml version="1.0" encoding="UTF-8"?>"""
            "$firstLine\n$xmlElements"
        }
    return tempfile(dir = rootFileTestDir).apply {
        printWriter().use { it.write(serializedXml) }
    }
}

internal interface XmlElement {

    fun element(name: String, block: XmlElement.() -> Unit)

    fun element(name: String, content: String)

    fun element(name: String, content: Float)
}

private class XmlElementImpl(
    private val name: String,
    private var content: Any? = null
) : XmlElement {

    private val innerElements = mutableListOf<XmlElementImpl>()

    override fun element(name: String, block: XmlElement.() -> Unit) {
        innerElements += XmlElementImpl(name).apply(block)
    }

    override fun element(name: String, content: String) {
        elementContent(name, content)
    }

    private fun elementContent(name: String, content: Any) {
        innerElements += XmlElementImpl(name = name, content = content)
    }

    override fun element(name: String, content: Float) {
        elementContent(name, content)
    }

    fun serialize(): String {
        return when {
            innerElements.isNotEmpty() && content != null -> {
                error("XML element can't contain both value and inner elements")
            }
            content != null -> "<$name>$content</$name>"
            innerElements.isNotEmpty() -> {
                val serializedInnerElements = innerElements.joinToString("\n") { it.serialize() }
                """
                <$name>
                $serializedInnerElements
                </$name>
                """.trimIndent()
            }
            else -> "<$name/>"
        }
    }
}
