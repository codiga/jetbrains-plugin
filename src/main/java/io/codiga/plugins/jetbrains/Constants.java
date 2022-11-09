package io.codiga.plugins.jetbrains;

public class Constants {
    public final static String FRONTEND_URL = "https://app.codiga.io";

    public final static String PLUGIN_ID = "io.codiga.plugins.jetbrains-plugin";

    public static final String LOGGER_NAME = "Codiga";
    public static final String LINE_SEPARATOR = "\n";
    public static final char CHARACTER_SPACE = ' ';
    public static final char CHARACTER_TAB = '\t';

    public static final int NUMBER_OF_RECIPES_TO_KEEP_FOR_COMPLETION = 5;
    public static final int MINIMUM_LINE_LENGTH_TO_TRIGGER_AUTOCOMPLETION = 2;


    // Python-specific constants
    public static final String PYTHON_IMPORT_KEYWORD = "import";
    public static final String PYTHON_COMMENT_CHARACTER = "#";
    public static final String PYTHON_DEPENDENCY_FILE = "requirements.txt";


    // Java constants
    public static final String JAVA_PACKAGE_KEYWORD = "package";

    // Scala constants
    public static final String SCALA_PACKAGE_KEYWORD = "package";

    // JavaScript constants
    public static final String JAVASCRIPT_DEPENDENCY_FILE = "package.json";

    // Ruby constants
    public static final String RUBY_DEPENDENCY_FILE = "Gemfile";

    // PHP constants
    public static final String PHP_DEPENDENCY_FILE = "composer.json";
}
