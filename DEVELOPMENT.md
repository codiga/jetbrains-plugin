# Development Guide

This document provides insight into the inner workings on this JetBrains IDE plugin. For a higher-level,
end-user documentation you can refer to the [extension's online documentation](https://doc.codiga.io/docs/code-analysis/ide/jetbrains/).

## Architecture

The plugin is a single-module project with the following structure:

```
jetbrains-plugin
    - images                                    <-- Assets for documentation
    - src 
        - main
            - graphql                           <-- GraphQL resources: queries, mutations, schema
            - java
                - icons                         <-- IntelliJ Platform specific code to retrieve icons
                - io.codiga.plugins.jetbrains   <-- Plugin sources 
            - resources
                - icons                         <-- Icon assets
                - inspectionDescriptions        <-- Resources to provide inspection descriptions in the IDE settings 
                - META-INF                      <-- Plugin configuration files 
                - schema                        <-- YAML schema for codiga.yml 
        - test
            - data                              <-- Test data
            - java
            - resources
```

## codiga.yml configuration

To enable the Rosie service in a project, one has to create and configure a `codiga.yml` file in the project's root directory.
This is the file in which you can tell Rosie what rulesets you want to use, or ignore in that given project.

Details on what the configuration can hold can be found at [Code Analysis for JetBrains](https://doc.codiga.io/docs/code-analysis/ide/jetbrains/#the-configuration-file).

## Rosie cache

The plugin incorporates an internal cache ([`RosieRulesCache`](/src/main/java/io/codiga/plugins/jetbrains/annotators/RosieRulesCache.java))
of all the rules from the rulesets that are specified in the rulesets property in `codiga.yml`,
along with a periodic update mechanism. This caching makes it possible to provide better performance.

The cache is initialized and the periodic update begins when a new project is opened
(see [`RosieStartupActivity`](/src/main/java/io/codiga/plugins/jetbrains/starter/RosieStartupActivity.java)
and [`RosieRulesCacheUpdateHandler`](/src/main/java/io/codiga/plugins/jetbrains/starter/RosieRulesCacheUpdateHandler.java)).
Each project has its own separate project-level cache (as a project-level service), and background thread to update that cache. 

The periodic update is executed in every 10 seconds, and updates the cache if either the `codiga.yml` file has changed,
or the configured rulesets (or underlying rules) have changed on Codiga Hub.

The cache is updated according to the content of the `codiga.yml` file serialized by Jackson into a [`CodigaYmlConfig`](/src/main/java/io/codiga/plugins/jetbrains/rosie/model/codiga/CodigaYmlConfig.java) instance.

The backround thread for a specific project is terminated upon the user closing that project. 

## Rosie client

[`RosieApiImpl`](/src/main/java/io/codiga/plugins/jetbrains/rosie/RosieApiImpl.java) is responsible for the communication between the plugin and the Rosie service.

It sends information (document text, document language, Rosie rules cached for the given language, etc.) to Rosie,
then processes the returned response (code violations, ranges, severities, etc.) and supplies it to the external annotator functionality.

The model objects for serializing the response data can be found in the [`io.codiga.plugins.jetbrains.model.rosie`](/src/main/java/io/codiga/plugins/jetbrains/model/rosie) package.

## Annotations

Highlighting of the code violations and providing quick fixes for them is implemented via an external annotator called [`RosieAnnotator`](/src/main/java/io/codiga/plugins/jetbrains/annotators/RosieAnnotator.java)
that consumes [`RosieAnnotation`](/src/main/java/io/codiga/plugins/jetbrains/model/rosie/RosieAnnotation.java) objects created by `RosieApiImpl` from a `RosieResponse`.

The `RosieAnnotation` objects are then filtered and mapped to [`RosieAnnotationJetBrains`](/src/main/java/io/codiga/plugins/jetbrains/model/rosie/RosieAnnotationJetBrains.java) instances
so that the editor offset values are resolved from Codiga's start/end line and column values, and can be processed by the IDE.

It provides three different quick fixes:
- **Fix: &lt;fix description>**: applies an actual code fix for the violation. See [`RosieAnnotationFix`](/src/main/java/io/codiga/plugins/jetbrains/annotators/RosieAnnotationFix.java).
- **Remove error '&lt;rule name>'**: disables Codiga code analysis for the line on which the violation occurred. See [`DisableRosieAnalysisFix`](/src/main/java/io/codiga/plugins/jetbrains/annotators/DisableRosieAnalysisFix.java).
- **See rule '&lt;rule name>' on the Codiga Hub**: opens the related rule on Codiga Hub. See [`AnnotationFixOpenBrowser`](/src/main/java/io/codiga/plugins/jetbrains/annotators/AnnotationFixOpenBrowser.java).

## GraphQL client, queries and mutations

The plugin uses a GraphQL client to send queries and mutations to Codiga.

The queries are used to fetch timestamp-, snippet- and ruleset related data from Codiga, while mutations are used to send metrics to Codiga
of the usage of certain functionality, for example when a Rosie fix is applied.

During building the plugin, the GraphQL Apollo client generates Java classes based on the corresponding [`schema.json`](/src/main/graphql/io/codiga/api/schema.json),
which types are used throughout the plugin for integrating with Codiga.

### User-Agent

The User-Agent header is sent in order to identify the client application the GraphQL requests are sent from.

It is in the form `<product name>/<major>.<minor>`, e.g. `IntelliJ IDEA/2022.1` and
is retrieved in [`UserAgentUtils`](/src/main/java/io/codiga/plugins/jetbrains/utils/UserAgentUtils.java).

### User fingerprint

In general, the fingerprint is a unique string generated when the plugin is installed.

### Codiga API Token

Having a Codiga account registered, using this token, users can access and use to their private rulesets and rules in the IDE.

The configuration is provided in the Codiga specific application-level IDE settings via [`AppSettingsState`](/src/main/java/io/codiga/plugins/jetbrains/settings/application/AppSettingsState.java).

## Environments

In case testing on different environments is necessary, you can use the following endpoints:

| Environment | Codiga                                | Rosie                                      |
|-------------|---------------------------------------|--------------------------------------------|
| Production  | https://api.codiga.io/graphql         | https://analysis.codiga.io/analyze         |
| Staging     | https://api-staging.codiga.io/graphql | https://analysis-staging.codiga.io/analyze |

## Testing

In this plugin, tests are a mix of unit and integration tests.

There is a base test class called [`TestBase`](/src/test/java/io/codiga/plugins/jetbrains/testutils/TestBase.java) that is extended by most,
if not all, test classes.

When you do integration testing that needs a project root folder and documents to interact with, override
the `getTestDataRelativePath()` in the specific test class, e.g.:

```java
@Override
protected String getTestDataRelativePath() {
    return TEST_DATA_BASE_PATH + "/rosiecacheupdater";
}
```

and create the corresponding folder under `src/test/data`, and add documents to it. That folder will act as
if it was an actual project root directory.

Tests can be executed either individually via gutter icons or invoking the **Run Tests** run configuration (executes `gradle test`).

General IntelliJ Platform related testing instructions can be found in their [Plugin SDK documentation](https://plugins.jetbrains.com/docs/intellij/testing-plugins.html).

## How to guide

### Build and run the plugin

There are predefined Run Configurations that you can use:
- **Run Build Plugin** to build the plugin
- **Run Plugin** to run/debug the plugin

### Updating the GraphQL Schema

See instructions [here](https://www.apollographql.com/docs/android/essentials/get-started-java/)

```shell
./gradlew :downloadApolloSchema --endpoint='https://api.codiga.io/graphql' --schema=schema.json
```

This downloads the up-to-date `schema.json` file into the project root, with what you have to replace the existing `schema.json` to
have it updated.

### Add support for a new Rosie language

To simply support a new language:
- Add the new language to the set of languages supported by Rosie in [`RosieLanguageSupport#SUPPORTED_LANGUAGES`](/src/main/java/io/codiga/plugins/jetbrains/utils/RosieLanguageSupport.java).
- Also in `RosieLanguageSupport`, add a mapping between the language enumeration and the Rosie language id
(most probably the all lowercase version of the language enum constant), in `getRosieLanguage(LanguageEnumeration)`.

If default ruleset suggestions are also needed for the language, then:
- Add the new language's file extension(s) to `FILE_EXTENSIONS` in [`CodigaRulesetConfigs`](/src/main/java/io/codiga/plugins/jetbrains/rosie/CodigaRulesetConfigs.java).
- Create a new `DEFAULT_<language>_RULESET_CONFIG` with the appropriate content and add it to `getDefaultRulesetsForProject()`.
- Depending on the language, and the type of support JetBrains has for that language, custom checks may be implemented to
see if a certain SDK is configured, so that querying the files in the project can be avoided.

### Add a new AST type for Rosie

- Make sure to update the GrahpQL `schema.json`.
- Create new `ENTITY_CHECKED_*` constants in [`RosieRuleAstTypes`](src/main/java/io/codiga/plugins/jetbrains/model/rosie/RosieRuleAstTypes.java)
and map them to `ElementCheckedEnumeration` constants in `elementCheckedToRosieEntityChecked()`.

## Debug logging

To enable *debug* log statements, and see them in the IDE's `idea.log` file, you have two options:

### Enable it via build.gradle.kts

Here, just uncomment the `runIde { systemProperty("idea.log.debug.categories", "Codiga") }` section.

### Enable it in the sandbox IDE

For this you need to:

- Run the sandbox IDE,
- Open the <kbd>Help</kbd> > <kbd>Diagnostic Tools</kbd> > <kbd>Debug Log Settings...</kbd> dialog,
- Enter *Codiga* on the first line. *Codiga* is the logger category to which our loggers are associated.
