package io.github.ackeecz.danger.dependenciescheck.model

internal fun createDependencyName(
    groupId: GroupId = GroupId("com.group.id"),
    artifactId: ArtifactId = ArtifactId("artifact-id"),
    version: Version = Version("1.0.0"),
): DependencyName {
    return DependencyName(
        groupId = groupId,
        artifactId = artifactId,
        version = version,
    )
}
