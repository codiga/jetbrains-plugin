package com.code_inspector.plugins.intellij;

import com.code_inspector.plugins.intellij.annotators.CodeInspectionAnnotation;
import com.google.common.collect.ImmutableList;

public class Constants {
    public static Long INVALID_PROJECT_ID = 0L;
    public static final String LOGGER_NAME = "CodeInspector";
    public static final java.util.List<CodeInspectionAnnotation> NO_ANNOTATION = ImmutableList.of();
}
