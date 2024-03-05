package io.github.ackeecz.danger.dependenciescheck.model.update

import io.github.ackeecz.danger.dependenciescheck.model.DependencyName
import io.github.ackeecz.danger.dependenciescheck.model.Version
import io.github.ackeecz.danger.dependenciescheck.model.createDependencyName

internal fun createDependenciesUpdateReport(
    outdatedDependencies: List<OutdatedDependency> = emptyList(),
    upToDateDependencies: List<UpToDateDependency> = emptyList(),
): DependenciesUpdateReport {
    return DependenciesUpdateReport(
        outdatedDependencies = outdatedDependencies,
        upToDateDependencies = upToDateDependencies,
    )
}

internal fun createOutdatedDependency(
    name: DependencyName = createDependencyName(),
    newestAvailableVersion: Version = Version("2.0.0"),
): OutdatedDependency {
    return OutdatedDependency(
        name = name,
        newestAvailableVersion = newestAvailableVersion,
    )
}

internal fun createUpToDateDependency(
    name: DependencyName = createDependencyName(),
): UpToDateDependency {
    return UpToDateDependency(name = name)
}
