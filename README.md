[ ![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-dependencies-check/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.ackeecz/danger-kotlin-dependencies-check)

# danger-kotlin dependencies check plugin

Plugin for [danger-kotlin](https://github.com/danger/kotlin) for checking project dependencies (e.g. new available updates or vulnerabilities). Plugin 
relies on these Gradle plugins to be available on the project:
* https://github.com/jeremylong/DependencyCheck
* https://github.com/ben-manes/gradle-versions-plugin

Plugin runs Gradle tasks above, collects results and reports outdated dependencies as warnings and vulnerable
dependencies as warnings if there is no update available, the dependency is transitive (update is unknown) or fails
pipeline if there is a vulnerability and update to a newer version is available. All of this can be suppressed for
cases such as false positives or other valid reasons.

## Installation

Put

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-dependencies-check:x.y.z")
```

to the top of your Dangerfile

## Usage

First you need to register the plugin via

```kotlin
register plugin DependenciesCheckPlugin
```

and then you can use it through it's single public method

```kotlin
DependenciesCheckPlugin.checkDependencies(config)
```

`checkDependencies` method accepts `Config` object where you can specify various configurations of the plugin such as
suppressions of outdated dependencies or vulnerabilities reports. See `io.github.ackeecz.danger.dependenciescheck.config.Config` 
class for more details.

Example Dangerfile

```kotlin
@file:DependsOn("io.github.ackeecz:danger-kotlin-dependencies-check:x.y.z")

import io.github.ackeecz.danger.dependenciescheck.config.Config
import io.github.ackeecz.danger.dependenciescheck.config.OutdatedDependencySuppression
import io.github.ackeecz.danger.dependenciescheck.DependenciesCheckPlugin

import systems.danger.kotlin.danger
import systems.danger.kotlin.register

register plugin DependenciesCheckPlugin

danger(args) {
    val config = Config(
        outdatedDependenciesConfig = Config.OutdatedDependencies(
            suppressions = listOf(
                OutdatedDependencySuppression(fullyQualifiedNameWithVersion = "com.squareup.retrofit2:retrofit:2.4.0"),
            ),
        ),
    )
    DependenciesCheckPlugin.checkDependencies(config)
}
```

This will perform dependencies check and configures a plugin to suppress an outdated dependency report for Retrofit.
