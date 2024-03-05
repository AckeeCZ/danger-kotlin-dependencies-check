package io.github.ackeecz.danger.dependenciescheck.model.xml.update

import io.github.ackeecz.danger.dependenciescheck.model.ArtifactId
import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.GroupId
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.update.UpToDateDependency
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class XmlUpToDateDependencyTest : FunSpec({

    test("map xml dependency to higher-level representation") {
        val groupId = "com.squareup.retrofit2"
        val artifactId = "retrofit2"
        val version = "2.4.0"
        val underTest = XmlUpToDateDependency(
            groupId = groupId,
            artifactId = artifactId,
            currentVersion = version,
        )

        underTest.toUpToDateDependency() shouldBe UpToDateDependency(
            name = DependencyName(
                groupId = GroupId(groupId),
                artifactId = ArtifactId(artifactId),
                version = Version(version),
            ),
        )
    }
})
