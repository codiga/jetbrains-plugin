package io.codiga.plugins.jetbrains;

import io.codiga.plugins.jetbrains.annotators.Annotation;
import com.google.common.collect.ImmutableList;

public class Constants {
    public final static String FRONTEND_URL = "https://app.codiga.io";

    public static Long INVALID_PROJECT_ID = 0L;
    public static final String LOGGER_NAME = "Codiga";
    public static final String LINE_SEPARATOR = "\n";
    public static final char CHARACTER_SPACE = ' ';

    public static final int NUMBER_OF_RECIPES_TO_KEEP_FOR_COMPLETION = 3;
    public static final int MINIMUM_LINE_LENGTH_TO_TRIGGER_AUTOCOMPLETION = 5;


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

    public static final java.util.List<Annotation> NO_ANNOTATION = ImmutableList.of();
    public static final long REAL_TIME_FEEDBACK_TIMEOUT_MILLIS = 15 * 1000 ; // 15 seconds
    public static final long SLEEP_BETWEEN_FILE_ANALYSIS_MILLIS = 200;
    public static final long FILE_ANALYSIS_INITIAL_SLEEP_MILLIS = 1200;
}
