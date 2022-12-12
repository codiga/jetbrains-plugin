# Contributing

## Issues

If you find a bug, please report it in the GitHub issue tracker.

## Pull Requests

If you would like to contribute to the plugin, please
send a Pull Request on GitHub. The Pull Request will be analyzed
by Code Inspector and reviewed by a Codiga engineer.

## DOs and DON'Ts

### DO:

* Follow our coding style
* Add labels to your issues and pull requests (at least one label for each of Status/Type/Priority).
* Give priority to the current style of the project or file you're changing, even if it diverges from the general
  guidelines.
* Include tests when adding new features as much as possible. When fixing bugs, start with adding a test that highlights
  how the current behavior is broken.
* Keep the discussions focused. When a new or related topic comes up, it's often better to create a new issue than to
  side track the discussion.
* Run all Gradle verification tasks (`./gradlew check`) and test your code before submitting a pull request.

### DON'T:

* Send PRs for style changes.
* Send big PRs. Instead, file an issue and start a discussion so that we can agree on a direction before you invest a
  large amount of time.
* Commit code that you didn't write. If you find code that you think is a good fit, file an issue and start a discussion
  before proceeding.
* Submit PRs that alter licensing related files or headers. If you believe there's a problem with them, file an issue
  and we'll be happy to discuss it.

## Code Style

We follow the [Google Java Code Style](https://google.github.io/styleguide/javaguide.html).
We sometimes derive from it and that's okay.

## Updating the GraphQL Schema

See instructions [here](https://www.apollographql.com/docs/android/essentials/get-started-java/)

```shell
./gradlew :downloadApolloSchema --endpoint='https://api.codiga.io/graphql' --schema=schema.json
```

## Debug logging

To enable *debug* log statements, and see them in the IDE's `idea.log` file, you have two options:

### Enable it via build.gradle.kts

Here, just uncomment the `runIde { systemProperty("idea.log.debug.categories", "Codiga") }` section.

### Enable it in the sandbox IDE

For this you need to:

- Run the sandbox IDE,
- Open the <kbd>Help</kbd> > <kbd>Diagnostic Tools</kbd> > <kbd>Debug Log Settings...</kbd> dialog,
- Enter *Codiga* on the first line. *Codiga* is the logger category to which our loggers are associated.

## Publish new version

1. Update the version number in `gradle.properties` (see properties `pluginVersion`)
2. Push the code with the new version number
3. Wait for the GitHub Actions to be finished about your new push
4. Go on GitHub and a new release with the new version number should be here. The GitHub tag must match the version tag
   in `gradle.properties`
5. Publish the draft release

## Relevant documentation

- [IntelliJ Plugin documentation](https://plugins.jetbrains.com/docs/intellij/basics.html)
    - [Persistence](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html)
    - [Inspection](https://plugins.jetbrains.com/docs/intellij/code-inspections.html)