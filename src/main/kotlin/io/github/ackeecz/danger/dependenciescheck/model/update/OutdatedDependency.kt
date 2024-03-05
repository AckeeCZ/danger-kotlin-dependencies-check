package io.github.ackeecz.danger.dependenciescheck.model.update

import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.Version

internal data class OutdatedDependency(
    val name: DependencyName,
    val newestAvailableVersion: Version,
)
