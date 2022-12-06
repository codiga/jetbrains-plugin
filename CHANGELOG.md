<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Code Inspector JetBrains PlugIn Changelog

## Unreleased

### Added
- [#213](https://github.com/codiga/jetbrains-plugin/issues/213): Added support for Jupyter Notebook (.ipynb) files. 

### Changed

### Deprecated

### Removed

### Fixed

### Security

## 1.8.2 - 2022-11-09

### Added
- [#191](https://github.com/codiga/jetbrains-plugin/issues/191): Users can now navigate from codiga.yml to the rulesets on Codiga Hub
  via Ctrl + clicking on ruleset names.
- [#189](https://github.com/codiga/jetbrains-plugin/issues/189): Added an inspection to mark rulesets in codiga.yml when they either
  doesn't exist on Codiga Hub or they exist but contain no rule.

## 1.8.1

### Added
- [#177](https://github.com/codiga/jetbrains-plugin/issues/177): Added a notification popup, when we detect that the project,
  or one of its modules is configured with a Python SDK, to give users the option to create a Codiga config file with default Python rulesets.

### Changed
- Improved the performance of editor tab switches when the Snippet Search tool window is closed.

## 1.7.22

## 1.7.21

### Added
- Added JSON/YAML schema provider for the `codiga.yml` config file, so that code completion and automatic validation in that file is performed automatically.

### Changed
- Improved the plugin's settings UI.

### Fixed
- Fixed a UI issue on the plugin's settings page that the checkboxes did not reflect Codiga's disabled state.
- Fixed the issue that the status bar icon's tooltip text did not update upon enabling/disabling Codiga.

## 1.7.20

### Added
- [#158](https://github.com/codiga/jetbrains-plugin/issues/158): Added a quick fix for Rosie violations, to disable Rosie analysis for a specific row.

### Changed
- [#160](https://github.com/codiga/jetbrains-plugin/issues/160): Inline completion is no longer triggered with comments containing the `todo` or `fixme` keywords

## 1.7.19

### Added
- [#157](https://github.com/codiga/jetbrains-plugin/issues/157), [#141](https://github.com/codiga/jetbrains-plugin/issues/141): Support for codiga.yml file.

## 1.7.18

### Added
- [#142](https://github.com/codiga/jetbrains-plugin/issues/142): Added syntax highlighting for the code snippets in the Snippet Search
  tool window. Currently supported languages are Python and Java.

## 1.7.17

## 1.7.16

### Fixed
- [#150](https://github.com/codiga/jetbrains-plugin/issues/150): Added a missing library dependency and fixed `NoClassDefFoundError`s in some IDEs.

## 1.7.15

## 1.7.14

## 1.7.13

## 1.7.12

## 1.7.11

## 1.7.10

## 1.7.8

## 1.7.7

## 1.7.6

## 1.7.5

## 1.7.4

## 1.7.3

## 1.7.2

## 1.7.1

## 1.6.9

## 1.6.8

## 1.6.7

## 1.6.6

## 1.6.5

## 1.6.4

## 1.6.2

## 1.6.1

## 1.6.0

## 1.5.3

## 1.5.2

## 1.5.1

## 1.4.4

## 1.4.3

## 1.4.2

## 1.4.1

## 1.4.0

## 1.3.2

## 1.3.1

## 1.3.0

## 1.1.9

## 1.1.8

## 1.1.7

### Added
- Coding assistant support for Haskell, Solidity, Css, HTML and much more.

## 1.1.6

## 1.1.3

## 1.1.1

## 1.1.0

## 1.0.11

### Fixed
- Exception with bug on value not present

## 1.0.10

## 1.0.9

## 1.0.8

### Security
- Ability to analyze files without API keys

## 1.0.7

### Added
- remove duplicated errors that have the same message

### Changed
- migrate `org.jetbrains.intellij` to `1.1.2`
- use the document manager to get offset, which should
  lead to more accurate annotations.

## 1.0.6

### Added
- Support for Apex with Illuminated Cloud

## 1.0.5

### Added
- Support for IntelliJ 2021.1.3

## 1.0.4

### Added
- Correctly fixing the project identifier when analyzing a file (Fixes: #32)
- Fixes crash when trying to check what project has been notified (Fixes: #31)
- Use Real-Time Feedback to analyze all files, even when project exists
- Add the AccessRecord call when creating a file analysis

### Fixed
- Fix exception when sending notification to project (Fixes: #31)
- Updating dependencies

## 1.0.2

### Added
- Add long and complex functions detection
- Support new builds

## 1.0.1

### Added
- Add long and complex functions detection

## 1.0.0 - 2021-03-17

### Added
- Initial release
- Detect violation in IntelliJ
- No support for complex/long functions or duplicates.
