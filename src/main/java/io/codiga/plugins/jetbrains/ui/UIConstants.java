package io.codiga.plugins.jetbrains.ui;

public final class UIConstants {

    //API Token

    public static final String SETTINGS_CODIGA_ACCOUNT_SECTION_TITLE = "Codiga Account";
    public static final String SETTINGS_TEST_API_BUTTON_TEXT = "Test API connection";
    public static final String SETTINGS_GET_API_TOKEN_BUTTON_TEXT = "Get API token";
    public static final String SETTINGS_API_TOKEN_LABEL = "API Token:";
    public static final String SETTINGS_API_TOKEN_COMMENT = "Add your Codiga API key to use your recipes in your IDE.";
    public static final String SETTINGS_SNIPPETS_VISIBILITY_PARAMETERS = "Default Snippets Search when logged in";

    //Enable completions

    public static final String SETTINGS_CODE_AND_INLINE_COMPLETION_SECTION_TITLE = "Code and Inline Completion";
    public static final String SETTINGS_ENABLED_CODIGA = "Enable Codiga";
    public static final String SETTINGS_ENABLED_INLINE_COMPLETION = "Enable Coding Assistant with inline completion";
    public static final String SETTINGS_ENABLED_COMPLETION = "Enable Coding Assistant on Code Completion";

    //Snippet visibility

    public static final String SETTINGS_SNIPPETS_VISIBILITY_ALL_SNIPPETS = "All Snippets";
    public static final String SETTINGS_SNIPPETS_VISIBILITY_PUBLIC_SNIPPETS_ONLY = "Public Snippets Only";
    public static final String SETTINGS_SNIPPETS_VISIBILITY_PRIVATE_ONLY = "Private Snippets Only";
    public static final String SETTINGS_SNIPPETS_VISIBILITY_FAVORITE_ONLY = "Favorite Snippets Only";

    //API status

    public static final String API_STATUS_TITLE = "API Connection status";
    public static final String API_STATUS_TEXT_OK = "Connection Successful";
    public static final String API_STATUS_TEXT_FAIL =
        "Connection Failed. Check your access/secret keys and make sure you Apply changes before testing";

    public static final String ANNOTATION_PREFIX = "Codiga";

    private UIConstants() {
        //Utility class
    }
}
