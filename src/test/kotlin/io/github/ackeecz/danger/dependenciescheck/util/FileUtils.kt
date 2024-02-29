package io.github.ackeecz.danger.dependenciescheck.util

import io.kotest.core.TestConfiguration
import io.kotest.engine.spec.TempDirDeletionException
import io.kotest.engine.spec.TempFileDeletionException
import java.io.File
import kotlin.io.path.createTempDirectory

internal val rootFileTestDir: File
    get() {
        val projectRootDirectory = File(System.getProperty("user.dir"))
        return File(projectRootDirectory, "build")
    }

internal fun TestConfiguration.tempdir(parent: File, prefix: String? = null): File {
    val dir = createTempDirectory(parent.toPath(), prefix ?: javaClass.name).toFile()
    afterSpec {
        if (!dir.deleteRecursively()) throw TempDirDeletionException(dir)
    }
    return dir
}

internal fun TestConfiguration.tempfile(dir: File, prefix: String? = null, suffix: String? = null): File {
    val file = kotlin.io.path.createTempFile(dir.toPath(), prefix ?: javaClass.name, suffix).toFile()
    afterSpec {
        if (!file.delete()) throw TempFileDeletionException(file)
    }
    return file
}
