package io.github.ackeecz.danger.dependenciescheck.model.update

internal data class DependenciesUpdateReport(
    val outdatedDependencies: List<OutdatedDependency>,
    val upToDateDependencies: List<UpToDateDependency>,
)
