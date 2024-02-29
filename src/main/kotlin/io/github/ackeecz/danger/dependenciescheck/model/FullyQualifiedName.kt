package io.github.ackeecz.danger.dependenciescheck.model

internal data class FullyQualifiedName(
    val groupId: GroupId,
    val artifactId: ArtifactId,
) {

    val value = "${groupId.value}:${artifactId.value}"
}
