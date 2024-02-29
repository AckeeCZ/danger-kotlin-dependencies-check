package io.github.ackeecz.danger.dependenciescheck.model

internal data class DependencyName(
    val groupId: GroupId,
    val artifactId: ArtifactId,
    val version: Version,
) {

    val fullyQualifiedName = FullyQualifiedName(groupId, artifactId)

    val fullyQualifiedNameWithVersion = "${fullyQualifiedName.value}:${version.value}"
}
