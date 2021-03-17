# Contributing

## Issues

If you find a bug, please report the bug in the GitHub issue tracker.

## Pull Requests

If you would like to contribute to the plugin, please
send a Pull Request on GitHub. The Pull Request will be analyzed
by Code Inspector and also by one of the Code Inspector engineer.


## DOs and DON'Ts

### DO:

* Follow our coding style
* Add labels to your issues and pull requests (at least one label for each of Status/Type/Priority).
* Give priority to the current style of the project or file you're changing, even if it diverges from the general guidelines.
* Include tests when adding new features as much as possible. When fixing bugs, start with adding a test that highlights how the current behavior is broken.
* Keep the discussions focused. When a new or related topic comes up, it's often better to create a new issue than to side track the discussion.
* Run all Gradle verification tasks (`./gradlew check`) and test your code before submitting a pull request.

### DON'T:

* Send PRs for style changes.
* Surprise us with big pull requests. Instead, file an issue and start a discussion so we can agree on a direction before you invest a large amount of time.
* Commit code that you didn't write. If you find code that you think is a good fit, file an issue and start a discussion before proceeding.
* Submit PRs that alter licensing related files or headers. If you believe there's a problem with them, file an issue and we'll be happy to discuss it.


## Code Style

We follow the [Google Java Code Style](https://google.github.io/styleguide/javaguide.html).
We sometimes derive from it and that's okay.


## Relevant documentation

 - [IntelliJ Plugin documentation](https://plugins.jetbrains.com/docs/intellij/basics.html)
   - [Persistence](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html)
   - [Inspection](https://plugins.jetbrains.com/docs/intellij/code-inspections.html)