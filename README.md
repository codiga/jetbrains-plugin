[![Code Grade](https://api.codiga.io/project/29692/status/svg)](https://app.codiga.io/public/project/29692/jetbrains-plugin/dashboard)
[![Code Quality](https://api.codiga.io/project/29692/score/svg)](https://app.codiga.io/public/project/29692/jetbrains-plugin/dashboard)
![JetBrains Plugins Download](https://img.shields.io/jetbrains/plugin/d/17969)
![JetBrains Plugins Ratings](https://img.shields.io/jetbrains/plugin/r/rating/17969)
![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/17969)

# Codiga for Jetbrains

Codiga is a tool to search, create and share [smart code snippets](https://www.codiga.io/code-snippets/smart-code-snippets/) from your IDE. It is integrated
with all JetBrains products as well as [VS Code](https://marketplace.visualstudio.com/items?itemName=codiga.vscode-plugin) and [Google Chrome](https://chrome.google.com/webstore/detail/codiga/dbkhkhonmelajjempmoadocgneoadjge).

Codiga is compatible with all JetBrains products: IntelliJ, AppCode, PHPStorm, DataDrip, GoLand, RubyMine, PyCharm, CLion (and many more).

With Codiga, you can create your own code snippets and find and import them easily.

![Inline Completion](images/inline.gif "Using an inline snippet")

## Installation

Codiga is available on the [Jetbrains marketplace](https://plugins.jetbrains.com/plugin/17969-codiga) 
and you can install it directly within any Jetbrains product (IntelliJ, PHPStorm, PYCharm, etc).

![Codiga Jetbrains plugin](images/plugin-description.png "Codiga PlugIn")


## Quick Start

Open a file, make a one-line comment to see all suggestions from the Codiga API.

By default, Codiga searches all public snippet. If you want Codiga to also
look for your own snippets, add your API keys (see configuration below).

## Using shortcuts in the IDE

Install the extension and type `.` in your IDE to list all available snippets for your current environment (language, file, libraries).

Accept a snippet using either Enter ↩ or Tab ↹. Go through the snippet variables using the Tab key.

![Using Code Snippet Shortcut](images/shortcut.gif "Using a shortcut")

## Snippet Search

Use the Codiga Snippets panel on the right to find code snippets that matches your environment.

![Code Snippet Search](images/snippet-search.gif "Code Snippet Search")

## Semantic Search

Press CTRL + ALT + C (or choose the menu option "Tools" → "Coding Assistant") to launch a request 
to Codiga and find snippets based on your keywords.

![Code Snippet Semantic Search](images/coding-assistant.gif "Coding Assistant")

## List all shortcuts

Press CTRL + ALT + S (or choose the menu option "Tools" → "Shortcuts") to list
all keywords for your environment.

![List all snippet shortcuts](images/shortcut-list.gif "List of all shortcuts")

## Creating and sharing snippets

![Create Code Snippet](images/create-recipe.gif "Creating Recipe")

1. Select the code in your editor
2. Right click on `Create Codiga Snippet`



## Connecting your Codiga Account

You can connect your Codiga account and benefit from all the Codiga features: 

 1. use the code snippets and cookbooks you define and subscribe to
 2. use all your code analysis preferences on Codiga

In order to link your Codiga account, you need to add your API token to your preferences.
First, go on our application and generate an API token as shown below.

![API Token Creation](images/api-token-creation.gif)


Then, enter your API token in the Jetbrains plugin configuration, as shown below.

![Project Configuration](images/api-token.png)

Once the token is added, click on "Apply" and then "Test API connection".

# Support

You can use the current [issue tracker](https://github.com/codiga/jetbrains-plugin/issues)
or report the issue on our [Slack channel](https://join.slack.com/t/codigahq/shared_invite/zt-9hvmfwie-9BUVFwZDwvpIGlkHv2mzYQ).

# Implementation Details

## Dependencies

 * [apollo-android](https://github.com/apollographql/apollo-android) 
   to access the [Code Inspector GraphQL API](https://doc.codiga.io/docs/api/)

## License

This project is under the GPL-3. See the LICENSE file for more information.

## Contact

To report a bug, submit an issue directly on the [GitHub issue tracker](https://github.com/codiga/jetbrains-plugin/issues).
