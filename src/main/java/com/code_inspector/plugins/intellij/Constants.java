package com.code_inspector.plugins.intellij;

import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.google.common.collect.ImmutableList;

public class Constants {
    public static Long INVALID_PROJECT_ID = 0L;
    public static final String LOGGER_NAME = "CodeInspector";
    public static final String LINE_SEPARATOR = "\n";
    public static final char CHARACTER_SPACE = ' ';
    public static final String PYTHON_IMPORT_KEYWORD = "import";
    public static final String PYTHON_COMMENT_CHARACTER = "#";
    public static final java.util.List<CodeInspectionAnnotation> NO_ANNOTATION = ImmutableList.of();
    public static final long REAL_TIME_FEEDBACK_TIMEOUT_MILLIS = 15 * 1000 ; // 15 seconds
    public static final long SLEEP_BETWEEN_FILE_ANALYSIS_MILLIS = 200;
    public static final long FILE_ANALYSIS_INITIAL_SLEEP_MILLIS = 1200;
}
