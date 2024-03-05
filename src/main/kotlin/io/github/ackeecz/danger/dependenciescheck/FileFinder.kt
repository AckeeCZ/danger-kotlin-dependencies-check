package io.github.ackeecz.danger.dependenciescheck

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

internal class FileFinder {

    fun findFiles(rootDirectoryPath: Path, fileName: String): List<File> {
        val maxDepth = 10
        return Files.find(rootDirectoryPath, maxDepth, { path, _ -> path.toFile().name == fileName })
            .map { it.toFile() }
            .collect(Collectors.toList())
    }
}
