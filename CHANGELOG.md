<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Code Inspector IntelliJ PlugIn Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.10]

### Fixed
 - Exception when violation line is 0


## [1.0.9]
### Added

 - Detect package and context for Javascript analysis

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.8]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.8]

- Ability to analyze files without API keys

## [1.0.7]

### Added
- remove duplicated errors that have the same message

### Changed
- migrate `org.jetbrains.intellij` to `1.1.2`
- use the document manager to get offset, which should
  lead to more accurate annotations.
  
### Deprecated

### Removed

### Fixed

### Security

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