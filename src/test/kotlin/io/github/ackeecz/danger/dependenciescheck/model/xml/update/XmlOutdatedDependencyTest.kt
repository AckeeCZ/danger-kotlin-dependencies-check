package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import io.github.ackeecz.danger.dependenciescheck.model.ArtifactId
import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.OutdatedDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class XmlOutdatedDependencyTest : FunSpec({

    test("map xml dependency to higher-level representation") {
        val groupId = "com.squareup.retrofit2"
        val artifactId = "retrofit2"
        val currentVersion = "2.4.0"
        val availableVersion = "2.9.0"
        val underTest = XmlOutdatedDependency(
            groupId = groupId,
            artifactId = artifactId,
            currentVersion = currentVersion,
            availableVersion = XmlAvailableVersion(version = availableVersion)
        )

        underTest.toOutdatedDependency() shouldBe OutdatedDependency(
            name = DependencyName(
                groupId = GroupId(groupId),
                artifactId = ArtifactId(artifactId),
                version = Version(currentVersion),
            ),
            newestAvailableVersion = Version(availableVersion),
        )
    }
})
