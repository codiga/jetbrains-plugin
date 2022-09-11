package io.codiga.plugins.jetbrains.utils;

import io.codiga.api.type.LanguageEnumeration;

public class RosieUtils {
    public static final String getRosieLanguage(LanguageEnumeration language) {
        switch (language) {
            case PYTHON:
                return "python";
            default:
                return "unknown";
        }
    }
}
