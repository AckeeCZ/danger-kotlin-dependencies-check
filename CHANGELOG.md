# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-03-05
### Added
- First version 🎉. Implements running Gradle tasks for checking outdated dependencies, possible updates and dependency
vulnerabilities. These Gradle plugins need to be configured on the project for this Danger plugin to run them, collect
results and make appropriate reports:
  * https://github.com/jeremylong/DependencyCheck
  * https://github.com/ben-manes/gradle-versions-plugin
