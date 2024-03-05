package io.github.ackeecz.danger.dependenciescheck

import io.github.ackeecz.danger.dependenciescheck.util.rootFileTestDir
import io.github.ackeecz.danger.dependenciescheck.util.tempdir
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import java.io.File

private lateinit var underTest: FileFinder

internal class FileFinderTest : FunSpec({

    beforeEach {
        underTest = FileFinder()
    }

    test("find files with a given name") {
        // Arrange
        val rootDir = rootFileTestDir
        val appDir = tempdir(rootDir, "app")
        val buildDir = tempdir(appDir, "build")
        val reportsDir = tempdir(buildDir, "reports")
        val contentDir1 = tempdir(reportsDir, "content1")
        val contentDir2 = tempdir(reportsDir, "content2")

        val fileName = "dependency-updates-report.xml"
        val expectedFirstFile = File(contentDir1, fileName).also { it.createNewFile() }
        val expectedSecondFile = File(contentDir2, fileName).also { it.createNewFile() }

        // Act
        val actualFiles = underTest.findFiles(rootDirectoryPath = rootDir.toPath(), fileName = fileName)

        // Assert
        actualFiles.size shouldBe 2
        actualFiles shouldContain expectedFirstFile
        actualFiles shouldContain expectedSecondFile
    }
})
