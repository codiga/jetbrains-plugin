<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Code Inspector JetBrains PlugIn Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.7.2]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.7.1]

## [1.6.9]

## [1.6.8]

## [1.6.7]

## [1.6.6]

## [1.6.5]

## [1.6.4]

## [1.6.2]

## [1.6.1]

## [1.6.0]

## [1.5.3]

## [1.5.2]

## [1.5.1]

## [1.4.4]

## [1.4.3]

## [1.4.2]

## [1.4.1]

## [1.4.0]

## [1.3.2]

## [1.3.1]

## [1.3.0]

## [1.1.9]

## [1.1.8]

## [1.1.7]
### Added
- Coding assistant support for Haskell, Solidity, Css, HTML and much more.

## [1.1.6]

## [1.1.3]

## [1.1.1]

## [1.1.0]

## [1.0.11]
### Fixed
- Exception with bug on value not present

## [1.0.10]

## [1.0.9]

## [1.0.8]
### Security
- Ability to analyze files without API keys

## [1.0.7]
### Added
- remove duplicated errors that have the same message

### Changed
- migrate `org.jetbrains.intellij` to `1.1.2`
- use the document manager to get offset, which should
  lead to more accurate annotations.

## [1.0.6]
### Added
- Support for Apex with Illuminated Cloud

## [1.0.5]
### Added
- Support for IntelliJ 2021.1.3

## [1.0.4]
### Added
- Correctly fixing the project identifier when analyzing a file (Fixes: #32)
- Fixes crash when trying to check what project has been notified (Fixes: #31)
- Use Real-Time Feedback to analyze all files, even when project exists
- Add the AccessRecord call when creating a file analysis

### Fixed
- Fix exception when sending notification to project (Fixes: #31)
- Updating dependencies

## [1.0.2]
### Added
- Add long and complex functions detection
- Support new builds

## [1.0.1]
### Added
- Add long and complex functions detection

## [1.0.0] - 2021-03-17
### Added
- Initial release
- Detect violation in IntelliJ
- No support for complex/long functions or duplicates.